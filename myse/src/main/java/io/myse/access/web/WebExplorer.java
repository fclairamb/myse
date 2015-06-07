package io.myse.access.web;

import io.myse.access.File;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.DBDescFile;
import io.myse.exploration.FileAnalyser;
import io.myse.exploration.SourceExplorer;
import javax.persistence.EntityManager;

public class WebExplorer extends SourceExplorer {

	private static final int NB_LINKS_TO_ANALYSE = 10;

	private final FileAnalyser indexer;

	public WebExplorer(long sourceId) {
		super(sourceId);
		indexer = new FileAnalyser(sourceId);
		// We make sure the indexer won't execute itself
		indexer.setReschedule(false);
	}

	private static final long MAX_ELAPSED_TIME = 10000;

	@Override
	protected void explorerRun(EntityManager em) throws Exception {

		SourceWeb src = ((SourceWeb) this.source);
		src.setEntityManager(em);

		int count = 0;
		int nbNew = 0;
		long before = System.currentTimeMillis();
		for (DBDescFile file : DBDescFile.listFilesAll(dbSource, false, NB_LINKS_TO_ANALYSE, em)) {
			FileWeb f1 = new FileWeb(file, src);
			file.performingAnalysis();
			LOG.info("Analysing {} / depth = {}", f1, f1.getDepth());
			if (indexFile(f1, em)) {
				nbNew++;
			}
			file.updateNextAnalysis();
			if (f1.exists()) {
				if (f1.getDepth() < src.getMaxDepth()) {
					for (File f2 : f1.listFiles()) {
						LOG.info("Found {}", f2);
						if (indexFile(f2, em)) {
							nbNew++;
						}
					}
				}
				count++;
			}
			if (System.currentTimeMillis() - before > MAX_ELAPSED_TIME) {
				LOG.info("{}: We spent too much time...", this);
				break;
			}
		}

		if (count == 0) {
			File rootDir = src.getRootDir();
			LOG.warn("Creating root file: " + rootDir);
			DBDescFile.getOrCreate(rootDir, em);
		}

		indexer.setEntityManager(em);
		indexer.run();

		if (nbNew == 0) {
			delay += 1000;
		} else if (nbNew > 1) {
			delay /= 2;
		}
	}

	private static final long PERIOD_MIN = 5000,
			PERIOD_MAX = 300000;

	@Override
	protected void after() {
		super.after();
		if (delay < PERIOD_MIN) {
			delay = PERIOD_MIN;
		} else if (delay > PERIOD_MAX) {
			delay = PERIOD_MAX;
		}
	}

	@Override
	public String toString() {
		return "WebExplorer";
	}

}
