package com.webingenia.cifstest.explore;

import com.webingenia.cifstest.access.AccessException;
import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.DBMgmt;
import com.webingenia.cifstest.db.model.DBDescFile;
import javax.persistence.EntityManager;

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
			for (DBDescFile desc : DBDescFile.listFiles(source.getDesc(), false, 30, em)) {
				analyseFile(desc, em);
			}
		} finally {

		}
	}

	private void analyseFile(DBDescFile desc, EntityManager em) {
		LOG.info("Analysing " + desc);
		em.getTransaction().begin();
		try {
			desc.performingAnalysis();
			File file = source.getFile(desc.getPath());
		} catch (Exception ex) {
			LOG.error("analyseFile", ex);
		} finally {
			em.getTransaction().commit();
		}
	}

}
