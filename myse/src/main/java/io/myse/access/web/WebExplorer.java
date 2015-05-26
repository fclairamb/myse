package io.myse.access.web;

import io.myse.access.File;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.DBDescFile;
import io.myse.exploration.SourceExplorer;
import javax.persistence.EntityManager;

public class WebExplorer extends SourceExplorer {

	private static final int NB_LINKS_TO_ANALYSE = 10;
	
	public WebExplorer(long sourceId) {
		super(sourceId);
	}

	@Override
	protected void explorerRun(EntityManager em) throws Exception {
		SourceWeb src = ((SourceWeb) this.source);
		src.setEntityManager(em);

		int count = 0;
		for (DBDescFile file : DBDescFile.listFilesAll(dbSource, false, NB_LINKS_TO_ANALYSE, em)) {
			FileWeb f1 = new FileWeb(file, src);
			file.performingAnalysis();
			LOG.info("Analysing {} / depth = {}", f1, f1.getDepth());
			indexFile(f1, em);
			file.updateNextAnalysis();
			if (f1.exists()) {
				if (f1.getDepth() < src.getMaxDepth()) {
					for (File f2 : f1.listFiles()) {
						LOG.info("Found {}", f2);
						indexFile(f2, em);
					}
				}
				count++;
			}
		}

		if (count == 0) {
			File rootDir = src.getRootDir();
			LOG.warn("Creating root file: " + rootDir);
			DBDescFile.getOrCreate(rootDir, em);
		}

		this.delay = 5000;
	}

	@Override
	public String toString() {
		return "WebExplorer";
	}

}
