package com.webingenia.myse.fileexplore;

import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescFile;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.embeddedes.ElasticSearch;
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
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
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
			for (DBDescFile desc : DBDescFile.listFiles(source.getDesc(), false, 200, em)) {
				analyseFile(desc, em, esClient);
			}
		} catch (Exception ex) {
			LOG.error("FileIndexer.run", ex);
		} finally {
			em.close();
		}
	}

	private final ParseContext context = new ParseContext();

	private static final String ES_DOC_TYPE = "Document";

	private void analyseFile(DBDescFile desc, EntityManager em, Client esClient) {
		LOG.info("Analysing " + desc);

		try {
			try {
				em.getTransaction().begin();
				desc.performingAnalysis();
				File file = source.getFile(desc.getPath());
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

				String id = ds.getId() + "_" + desc.getId();

				Map<String, Object> data = new HashMap<>();
				String title = metadata.get(TikaCoreProperties.TITLE);
				if (title == null || title.trim().isEmpty()) {
					title = desc.getPath();
					title = title.substring(title.lastIndexOf("/")+1);
				}
				data.put("title", title);
				data.put("path", file.getPath());
				data.put("source", ds.getId());
				data.put("date_mod", desc.getDateMod());
				data.put("content", contenthandler.toString());
				IndexRequest req = new IndexRequestBuilder(esClient).setIndex(ds.getEsIndexName()).setType(ES_DOC_TYPE).setId(id).setSource(data).request();
				IndexResponse response = esClient.index(req).actionGet();
				LOG.info("Index response: " + response);
			} finally {
				// Whatever happens, we save
				em.getTransaction().commit();
			}
		} catch (Exception ex) {
			LOG.error("analyseFile", ex);
		}
	}

}
