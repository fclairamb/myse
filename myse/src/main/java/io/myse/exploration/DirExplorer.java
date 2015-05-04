package io.myse.exploration;

import io.myse.Main;
import io.myse.access.AccessException;
import io.myse.access.File;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.Config;
import io.myse.db.model.DBDescFile;
import java.util.List;
import javax.persistence.EntityManager;

/**
 * Directory explorer. This explorer is a multi-pass explorer. It will look into
 * each directory of a source and list of the files.
 */
public class DirExplorer extends SourceExplorer {

	public DirExplorer(long sourceId) {
		super(sourceId);
		delay = 500;
	}

	private static final String PRE = "direxplorer.";

	public static final String CONF_LOG_NEW_DIRS = PRE + "log_new_dirs";

	public static final String CONF_NB_DIRS_TO_WATCH_PER_PASS = PRE + "nb_dirs_to_watch_per_pass";
	protected boolean confLogDirsExploration;
	protected int confNbDirsToWatchPerPass;

	@Override
	protected void fetchSettings() {
		super.fetchSettings(); //To change body of generated methods, choose Tools | Templates.
		confLogDirsExploration = Config.get(CONF_LOG_NEW_DIRS, true, true);
		confNbDirsToWatchPerPass = Config.get(CONF_NB_DIRS_TO_WATCH_PER_PASS, 5, true);
	}

	@Override
	public void explorerRun(EntityManager em) {
		try {
			if (!source.getDesc().doIndex()) {
				if (!source.getDesc().deleted()) {
					LOG.warn("Source got deleted! Cancelling...");
					cancel();
				}
				return;
			}

			List<DBDescFile> files = DBDescFile.listFilesAll(dbSource, true, confNbDirsToWatchPerPass, em);

			int nbFiles = 0;

			if (files.isEmpty()) {
				File rootDir = source.getRootDir();
				if (rootDir == null) {
					LOG.error("{}: Could not get a rootdir !", this);
					return;
				}

				DBDescFile df = getDbFile(rootDir, em);
				nbFiles += analyseFile(df, rootDir, em);
			}

			// We analyse all the previously listed dirs
			for (DBDescFile desc : files) {
				if (!Main.running()) {
					LOG.warn("Bye bye !");
					return;
				}
				nbFiles += analyseFile(desc, em);
			}

			if (nbFiles < 1) {
				delay += 50;
			} else if (nbFiles > 1) {
				delay -= 5000;
			}

		} catch (AccessException ex) {
			if (dbSource != null) {
				dbSource.setState(ex.state());
			}
			LOG.error("DirExplorer issue", ex);
			delay += 10000;
		} catch (Exception ex) {
			if (dbSource != null) {
				dbSource.setState(AccessException.AccessState.ERROR);
			}
			LOG.error("DirExplorer issue", ex);
			delay += 20000;
		}
	}

	private static final long PERIOD_MIN = 100, PERIOD_MAX = 600000;

	@Override
	protected void after() {
		if (delay < PERIOD_MIN) {
			delay = PERIOD_MIN;
		} else if (delay > PERIOD_MAX) {
			delay = PERIOD_MAX;
		}

		super.after(); //To change body of generated methods, choose Tools | Templates.
	}

	protected int analyseFile(DBDescFile desc, EntityManager em) throws Exception {
		return analyseFile(desc, source.getFile(desc.getPath()), em);
	}

	protected int analyseFile(DBDescFile desc, File file, EntityManager em) throws Exception {
		if (!Main.running()) {
			LOG.warn("Bye bye !");
			return 0;
		}
		if (confLogDirsExploration) {
			LOG.info("[{}] Analysing {} \"{}\" : {}", file.isDirectory() ? "dir" : "file", source, file.getPath(), file.getLastModified());
		}

		int nbNewFiles = 0;

//		if (desc.getLastModified() == null) {
//			nbNewFiles += 1;
//		}
		if (!file.exists()) {
			em.remove(desc);
			return 1;
		}

		desc.performingAnalysis();

		if (file.isDirectory()) {
			try {
				for (File f : file.listFiles()) {
					try {
						if (indexFile(f, em)) {
							nbNewFiles += 1;
						}
					} catch (AccessException ex) {
						LOG.warn("analyse.file", ex);
					}
				}
			} catch (AccessException ex) {
				LOG.warn("analyse.listing", ex);
			}
		}

		if (indexFile(desc, file, em)) {
			nbNewFiles += 1;
		}

		return nbNewFiles;
	}

	@Override
	public String toString() {
		return String.format("DirExplorer{%s}", source);
	}
}
