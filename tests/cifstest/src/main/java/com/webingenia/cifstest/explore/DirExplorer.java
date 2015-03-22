package com.webingenia.cifstest.explore;

import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.DBMgmt;
import com.webingenia.cifstest.db.model.DBDescFile;
import javax.persistence.EntityManager;

public class DirExplorer implements Runnable {

	private final Source source;

	public DirExplorer(Source source) {
		this.source = source;
	}

	@Override
	public void run() {
		LOG.info("DirExplorer on " + source + " : STARTING !");
		EntityManager em = DBMgmt.getEntityManager();
		try {
			em.getTransaction().begin();
			for (DBDescFile desc : DBDescFile.listFiles(source.getDesc(), true, 100, em)) {
				File file = source.getFile(desc.getPath());
				LOG.info("Analysing " + file.getPath() + "...");
				if (file.getModifiedDate().compareTo(desc.getLastModification()) > 0) {
					LOG.info("Dir " + desc.getPath() + " was modified !");
				}
				desc.setLastModification(file.getModifiedDate());
				em.persist(desc);
			}
			em.getTransaction().commit();
		} catch (Exception ex) {
			LOG.error("DirExplorer issue", ex);
		} finally {
			em.close();
		}
	}

}
