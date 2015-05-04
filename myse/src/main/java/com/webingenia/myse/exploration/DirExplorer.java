package com.webingenia.myse.exploration;

import com.webingenia.myse.Main;
import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.common.EventsNotifier;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.db.model.DBDescFile;
import javax.persistence.EntityManager;

public class DirExplorer extends SourceExplorer {

	public DirExplorer(long sourceId) {
		super(sourceId);
	}

	private static final String PRE = "direxplorer.";

	public static final String CONF_LOG_NEW_DIRS = PRE + "log_new_dirs";

	public static final String CONF_NB_FILES_TO_FETCH = PRE + "nb_files_to_fetch";

	protected boolean confLogDirsExploration;
	protected int confNbFilesToFetch;

	@Override
	protected void fetchSettings() {
		super.fetchSettings(); //To change body of generated methods, choose Tools | Templates.
		confLogDirsExploration = Config.get(CONF_LOG_NEW_DIRS, false, true);
		confNbFilesToFetch = Config.get(CONF_NB_FILES_TO_FETCH, 100, true);
	}

	private long MAX_RUNNING_TIME = 60000L;

	@Override
	public void explorerRun(EntityManager em) {
		long start = System.currentTimeMillis();
		try {
			fetchSettings();
			if (!source.getDesc().doIndex()) {
				return;
			}

//			// We always start by a root dir analysis
			File rootDir = source.getRootDir();
			if (rootDir == null) {
				LOG.error("{}: Could not get a rootdir !", this);
				return;
			}
			DBDescFile df = getDbFile(rootDir, em);
			analyseFile(source, df, rootDir, em, true);

			boolean again = true;

			for (int pass = 0; pass < 3 && again && (System.currentTimeMillis() - start) < MAX_RUNNING_TIME; pass++) {
				if (confLogDirsExploration) {
					LOG.info("Analysis pass {}", pass);
				}
				// We analyse all the previously listed dirs
				for (DBDescFile desc : DBDescFile.listFilesAll(dbSource, true, confNbFilesToFetch, em)) {
					if (!Main.running()) {
						LOG.warn("Bye bye !");
						return;
					}
					if (analyseFile(source, desc, em, true)) {
						again = true;
					}
				}
			}
		} catch (AccessException ex) {
			if (dbSource != null) {
				dbSource.setState(AccessException.AccessState.DENIED);
			}
			LOG.error("DirExplorer issue", ex);
		} catch (Exception ex) {
			if (dbSource != null) {
				dbSource.setState(AccessException.AccessState.ERROR);
			}
			LOG.error("DirExplorer issue", ex);
		}
	}

	private boolean analyseFile(Source source, DBDescFile desc, EntityManager em, boolean sub) throws Exception {
		return analyseFile(source, desc, null, em, sub);
	}

	private boolean analyseFile(Source source, DBDescFile desc, File file, EntityManager em, boolean sub) throws Exception {
		if (!Main.running()) {
			LOG.warn("Bye bye !");
			return false;
		}
		if (file == null) {
			file = source.getFile(desc.getPath());
		}
		boolean dir = file.isDirectory();
		if (confLogDirsExploration) {
			LOG.info("[{}] Analysing {} \"{}\" : {}", dir ? "dir" : "file", source, file.getPath(), file.getLastModified());
		}

		if (mustSkip(file)) {
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
				EventsNotifier.eventScanningNewDir(file);
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
							DBDescFile df = getDbFile(f, em);
							if (analyseFile(source, df, f, em, false)) {
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

		return again;
	}

	@Override
	public String toString() {
		return String.format("DirExplorer{%s}", source);
	}
}
