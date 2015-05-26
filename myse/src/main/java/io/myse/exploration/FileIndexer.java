package io.myse.exploration;

import io.myse.Main;
import io.myse.access.File;
import io.myse.access.Source;
import io.myse.common.EventsNotifier;
import static io.myse.common.LOG.LOG;
import io.myse.common.ReschedulingRunnable;
import io.myse.db.DBMgmt;
import io.myse.db.model.DBDescFile;
import io.myse.db.model.DBDescSource;
import io.myse.embeddedes.ElasticSearch;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.xml.sax.ContentHandler;

public class FileIndexer extends ReschedulingRunnable {

	private final long sourceId;

	public FileIndexer(long sourceId) {
		this.sourceId = sourceId;
		delay = 1000;
	}

	private final boolean reAnalyse = false;

	private static final int FILES_TO_FETCH = 10;

	DBDescSource dbSource;
	Source source;

	@Override
	public void actualRun() {
		try {
			EntityManager em = DBMgmt.getEntityManager();
			dbSource = DBDescSource.get(sourceId, em);
			if (dbSource == null || dbSource.deleted()) {
				cancel();
			} else if (!dbSource.doIndex()) {
				return;
			}
			source = Source.get(dbSource);
			em.getTransaction().begin();
			if (!source.getDesc().doIndex()) {
				return;
			}
			try (Client esClient = ElasticSearch.client()) {
				LOG.info("FileIndexer on " + source + " : STARTING !");
				List<DBDescFile> files = reAnalyse ? DBDescFile.listFilesAll(source.getDesc(), false, FILES_TO_FETCH, em) : DBDescFile.listFilesToAnalyse(dbSource, false, FILES_TO_FETCH, em);
				if (!files.isEmpty()) {
					delay /= 2;
					for (DBDescFile desc : files) {
						if (!Main.running()) {
							LOG.warn("Bye bye !");
							return;
						}
						analyseFile(source, desc, em, esClient);
					}
				} else {
					delay += 15000;
				}
			} finally {
				LOG.info("FileIndexer: ENDED ! / delay = {}", delay);
				em.getTransaction().commit();
				em.close();
			}
		} catch (Exception ex) {
			LOG.error("FileIndexer.run", ex);
			delay += 5000;
		}
	}

	private final ParseContext context = new ParseContext();

	public static final String ES_DOC_TYPE = "Document";

	public static final int VERSION = 1;

	private void analyseFile(Source source, DBDescFile desc, EntityManager em, Client esClient) {
		LOG.info("{}: Analysing {}", this, desc);

		try {
			try {
				File file = source.getFile(desc.getPath());

				if (!file.exists()) {
					// TODO: Delete ES doc as well
					EventsNotifier.eventDeletingFile(file);
					ActionFuture<DeleteResponse> delete = esClient.delete(new DeleteRequest(source.getDesc().getShortName(), ES_DOC_TYPE, desc.getDocId()));
					if (!delete.actionGet().isFound()) {
						LOG.warn("Document {}:{} ({}) could not be found in ES. This is not good !", source.getDesc().getShortName(), desc.getDocId(), desc.getPath());
					}
					em.remove(desc);
					return;
				}

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
				LOG.info("{}: Document {}:{} ({}) {} !", this, ds.getShortName(), docId, desc.getPath(), created ? "created" : "updated");
			} catch (Exception ex) {
				// If anything happens, we need to save it !
				desc.performingAnalysis();
				LOG.error(this + ": Eror analysing file " + desc, ex);
			}

		} catch (Exception ex) { // We must never fail here
			LOG.error("FileIndexer", ex);
		}
	}

	private static final long PERIOD_MIN = 100, PERIOD_MAX = 120000;

	@Override
	protected void after() {
		if (delay < PERIOD_MIN) {
			delay = PERIOD_MIN;
		} else if (delay > PERIOD_MAX) {
			delay = PERIOD_MAX;
		}
		super.after(); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String toString() {
		return String.format("FileIndexer{%s}", source);
	}

}
