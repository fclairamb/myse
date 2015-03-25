package com.webingenia.cifstest.explore;

import com.webingenia.cifstest.access.AccessException;
import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.DBMgmt;
import com.webingenia.cifstest.db.model.DBDescFile;
import com.webingenia.cifstest.db.model.DBDescSource;
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
		DBDescSource sd = source.getDesc();
		try {
			em.getTransaction().begin();

			{ // We always start by a root dir analysis
				DBDescFile df = DBDescFile.getOrCreate(source.getRootDir(), em);
				analyseFile(df, em, true);
			}

			boolean again = true;

			for (int pass = 0; pass < 1 && again; pass++) {
				LOG.info("Analysis pass {}", pass);
				// We analyse all the previously listed dirs
				for (DBDescFile desc : DBDescFile.listFiles(sd, true, 100, em)) {
					if (analyseFile(desc, em, true)) {
						again = true;
					}
				}
			}
		} catch (AccessException ex) {
			sd.setState(AccessException.AccessState.DENIED);
			LOG.error("DirExplorer issue", ex);
		} catch (Exception ex) {
			sd.setState(AccessException.AccessState.ERROR);
		} finally {
			em.getTransaction().commit();
			em.close();
		}
	}

	private boolean analyseFile(DBDescFile desc, EntityManager em, boolean sub) throws Exception {
		File file = source.getFile(desc.getPath());
		boolean dir = file.isDirectory();
		LOG.info("Analysing {} \"{}\" : {}", dir ? "dir" : "file", file.getPath(), file.getLastModified());

		boolean again = false;

		if (desc.getLastModified() == null) {
			again = true;
		}

		desc.setLastModified(file.getLastModified());

		if (dir) {
			if (sub) {
				try {
					desc.performingAnalysis();
					for (File f : file.listFiles()) {
						try {
							DBDescFile df = DBDescFile.getOrCreate(f, em);
							if (analyseFile(df, em, false)) {
								again = true;
							}
						} catch (AccessException ex) {
							LOG.warn("analyse.file: " + ex);
						}
					}
				} catch (AccessException ex) {
					LOG.warn("analyse.listing: " + ex);
				}
				// We don't have to analyse it again, it's done
//				desc.setToAnalyze(false);
			}
		}

		em.persist(desc);

		return again;
	}
}
