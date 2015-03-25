package com.webingenia.cifstest.explore;

import com.webingenia.cifstest.access.AccessException;
import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.DBMgmt;
import com.webingenia.cifstest.db.model.DBDescFile;
import java.io.InputStream;
import java.net.URLConnection;
import javax.persistence.EntityManager;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

public class FileIndexer implements Runnable {

	private final Source source;

	public FileIndexer(Source source) {
		this.source = source;
	}

	@Override
	public void run() {
		EntityManager em = DBMgmt.getEntityManager();
		try {
			LOG.info("FileIndexer on " + source + " : STARTING !");
			for (DBDescFile desc : DBDescFile.listFiles(source.getDesc(), false, 200, em)) {
				analyseFile(desc, em);
			}
		} catch (Exception ex) {
			LOG.error("FileIndexer.run", ex);
		} finally {
			em.close();
		}
	}

	private final ParseContext context = new ParseContext();

	private void analyseFile(DBDescFile desc, EntityManager em) {
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
				LOG.info("Mime: " + metadata.get(Metadata.CONTENT_TYPE));
				LOG.info("Title: " + metadata.get(TikaCoreProperties.TITLE));
				//System.out.println("content: " + contenthandler.toString());
			} finally {
				// Whatever happens, we save
				em.getTransaction().commit();
			}
		} catch (Exception ex) {
			LOG.error("analyseFile", ex);
		}
	}

}
