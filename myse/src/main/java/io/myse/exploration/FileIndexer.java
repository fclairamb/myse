package io.myse.exploration;

import io.myse.Main;
import io.myse.access.File;
import io.myse.access.Source;
import io.myse.common.EventsNotifier;
import static io.myse.common.LOG.LOG;
import io.myse.common.RunnableCancellable;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBDescFile;
import io.myse.db.model.DBDescSource;
import io.myse.embeddedes.ElasticSearch;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManager;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.xml.sax.ContentHandler;

public class FileIndexer extends RunnableCancellable {

	private final long sourceId;

	public FileIndexer(long sourceId) {
		this.sourceId = sourceId;
	}

	private final boolean reAnalyse = true;

	private static final int FILES_TO_FETCH = 100;

	DBDescSource dbSource;
	Source source;
	
	@Override
	public void run() {
		EntityManager em = DBMgmt.getEntityManager();
		dbSource = DBDescSource.get(sourceId, em);
		source = Source.get(dbSource);
		try (Client esClient = ElasticSearch.client()) {
			if (!source.getDesc().doIndex()) {
				return;
			}
			LOG.info("FileIndexer on " + source + " : STARTING !");
			for (DBDescFile desc : reAnalyse ? DBDescFile.listFilesAll(source.getDesc(), false, FILES_TO_FETCH, em) : DBDescFile.listFilesToAnalyse(dbSource, FILES_TO_FETCH, em)) {
				if (!Main.running()) {
					LOG.warn("Bye bye !");
					return;
				}
				analyseFile(source, desc, em, esClient);
			}
			LOG.info(("FileIndexer: ENDED !"));
		} catch (Exception ex) {
			LOG.error("FileIndexer.run", ex);
		} finally {
			em.close();
		}
	}

	private final ParseContext context = new ParseContext();

	public static final String ES_DOC_TYPE = "Document";

	public static final int VERSION = 1;

	private void analyseFile(Source source, DBDescFile desc, EntityManager em, Client esClient) {
		LOG.info("{}: Analysing {}", this, desc);

		try {
			try {
				em.getTransaction().begin();
				try {
					File file = source.getFile(desc.getPath());

					boolean skip = !desc.isToAnalyze();

					desc.performingAnalysis();

					if (skip) {
						LOG.info("No analysis necessary !");
						return;
					} else {
						EventsNotifier.eventIndexingFile(file);
					}

					InputStream is = file.getInputStream();
					ContentHandler contenthandler = new BodyContentHandler();
					Metadata metadata = new Metadata();
					metadata.set(Metadata.RESOURCE_NAME_KEY, file.getPath());
					Parser parser = new AutoDetectParser();
					try {
						parser.parse(is, contenthandler, metadata, context);
					} catch (Exception ex) {
						//LOG.warn("Analyser error", ex);
					}

					DBDescSource ds = source.getDesc();

					String docId = desc.getDocId();

					Map<String, Object> data = new HashMap<>();
					String title = metadata.get(TikaCoreProperties.TITLE);
					if (title == null || title.trim().isEmpty()) {
						title = desc.getName();
					}
					data.put("title", title);
					data.put("name", desc.getName());
					data.put("extension", desc.getExtension());
					data.put("size", file.getSize());
					data.put("path", file.getPath());
					data.put("source_id", ds.getId());
					data.put("source_short", ds.getShortName());
					data.put("date_mod", desc.getDateMod());
					data.put("_version", VERSION);
					{
						String content = contenthandler.toString();
						content = content.replace("\n", " ").replace("\r", " ").replace("\t", " ");
						data.put("content", content);
					}
					IndexRequest req = new IndexRequestBuilder(esClient).setIndex(ds.getShortName()).setType(ES_DOC_TYPE).setId(docId).setSource(data).request();
					boolean created = esClient.index(req).actionGet().isCreated();
					LOG.info("{}: Document {} {} !", this, docId, created ? "created" : "updated");
				} catch (Exception ex) {
					// If anything happens, we need to save it !
					desc.performingAnalysis();
					LOG.error("Eror analysing file " + desc, ex);
				}
			} finally {
				// Whatever happens, we save
				em.getTransaction().commit();
			}
		} catch (Exception ex) { // We must never fail here
			LOG.error("FileIndexer", ex);
		}
	}

	@Override
	public String toString() {
		return String.format("FileIndexer{%s}", source);
	}
	
}