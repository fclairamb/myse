package com.webingenia.myse.fileexplore;

import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.common.EventsNotifier;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescFile;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.ElasticSearch;
import com.webingenia.myse.tasks.Tasks;
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

public class FileIndexer implements Runnable {

	private final Source source;

	public FileIndexer(Source source) {
		this.source = source;
	}

	@Override
	public void run() {
		EntityManager em = DBMgmt.getEntityManager();
		Client esClient = ElasticSearch.client();
		try {
			LOG.info("FileIndexer on " + source + " : STARTING !");
			if (source.getDesc().deleted()) {
				LOG.info("Deleted !");
			}
			for (DBDescFile desc : DBDescFile.listFiles(source.getDesc(), false, 300, em)) {
				analyseFile(desc, em, esClient);
			}
		} catch (Exception ex) {
			LOG.error("FileIndexer.run", ex);
		} finally {
			em.close();
		}
	}

	private final ParseContext context = new ParseContext();

	public static final String ES_DOC_TYPE = "Document";
	public static final String ES_INDEX_NAME = "all";

	public static final int VERSION = 1;

	private void analyseFile(DBDescFile desc, EntityManager em, Client esClient) {
		LOG.info("Analysing " + desc);

		try {
			try {
				em.getTransaction().begin();
				try {
					File file = source.getFile(desc.getPath());

					boolean skip = !desc.isToAnalyze();

					desc.performingAnalysis();

					if (skip) {
						LOG.info("No analysis necessary !");
						// return
					} else {
						EventsNotifier.indexingFile(file);
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
						title = desc.getPath();
						title = title.substring(title.lastIndexOf("/") + 1);
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
					IndexRequest req = new IndexRequestBuilder(esClient).setIndex(ES_INDEX_NAME).setType(ES_DOC_TYPE).setId(docId).setSource(data).request();
					boolean created = esClient.index(req).actionGet().isCreated();
					LOG.info("Document {} {} !", docId, created ? "created" : "updated");
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

}
