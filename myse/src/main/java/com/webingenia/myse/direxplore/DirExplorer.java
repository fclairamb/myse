package com.webingenia.myse.direxplore;

import com.webingenia.myse.Main;
import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.common.EventsNotifier;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.common.RunnableCancellable;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.db.model.DBDescFile;
import com.webingenia.myse.db.model.DBDescSource;
import javax.persistence.EntityManager;

public class DirExplorer extends RunnableCancellable {

	private final Source source;

	public DirExplorer(Source source) {
		this.source = source;
	}

	private boolean confLogDirsExploration;
	private int confNbFilesToFetch;

	private void fetchSettings() {

		confLogDirsExploration = Config.get(CONF_LOG_NEW_DIRS, false, true);
		confNbFilesToFetch = Config.get(CONF_NB_FILES_TO_FETCH, 100, true);
	}

	private static final String PRE = "direxplorer.";

	public static final String CONF_LOG_NEW_DIRS = PRE + "log_new_dirs";

	public static final String CONF_NB_FILES_TO_FETCH = PRE + "nb_files_to_fetch";

	@Override
	public void run() {
		fetchSettings();
		LOG.info("DirExplorer on " + source + " : STARTING !");
		if (source.getDesc().deleted()) {
			LOG.info("Deleted !");
		}
		EntityManager em = DBMgmt.getEntityManager();
		DBDescSource sd = source.getDesc();
		try {
			em.getTransaction().begin();

			try { // We always start by a root dir analysis
				DBDescFile df = DBDescFile.getOrCreate(source.getRootDir(), em);
				analyseFile(df, em, true);
			} finally {
				em.getTransaction().commit();
			}

			boolean again = true;

			for (int pass = 0; pass < 3 && again; pass++) {
				if (confLogDirsExploration) {
					LOG.info("Analysis pass {}", pass);
				}
				// We analyse all the previously listed dirs
				em.getTransaction().begin();
				try {
					for (DBDescFile desc : DBDescFile.listFiles(sd, true, confNbFilesToFetch, em)) {
						if (!Main.running()) {
							LOG.warn("Bye bye !");
							return;
						}
						if (analyseFile(desc, em, true)) {
							again = true;
						}
					}
				} finally {
					em.getTransaction().commit();
				}
			}
		} catch (AccessException ex) {
			sd.setState(AccessException.AccessState.DENIED);
			LOG.error("DirExplorer issue", ex);
		} catch (Exception ex) {
			sd.setState(AccessException.AccessState.ERROR);
		} finally {
			em.close();
		}
	}

	private boolean analyseFile(DBDescFile desc, EntityManager em, boolean sub) throws Exception {
		if (!Main.running()) {
			LOG.warn("Bye bye !");
			return false;
		}
		File file = source.getFile(desc.getPath());
		boolean dir = file.isDirectory();
		if (confLogDirsExploration) {
			LOG.info("[{}] Analysing {} \"{}\" : {}", dir ? "dir" : "file", source, file.getPath(), file.getLastModified());
		}

		String name = file.getName();

		// We don't care about this
		if (name.startsWith("~$") || name.startsWith(".")) {
			return false;
		}

		boolean again = false;

		if (desc.getLastModified() == null) {
			again = true;
		}

		desc.setLastModified(file.getLastModified());
		desc.setDirectory(dir);
		if (!dir) {
			desc.setSize(file.getSize());
		}

		desc.updateNextAnalysis();

		if (dir) {
			if (again) {
				EventsNotifier.scanningNewDir(file);
			}
			if (sub) {
				try {
					desc.performingAnalysis();
					for (File f : file.listFiles()) {
						if (!Main.running()) {
							LOG.warn("Bye bye !");
							return false;
						}
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
					if (ex.state() == AccessException.AccessState.UNKNOWN) {

					}
				}
			}
		}

		em.persist(desc);

		return again;
	}
}
