package com.webingenia.myse.access.drive;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.exploration.SourceExplorer;
import javax.persistence.EntityManager;

/**
 * Google Drive explorer. It should:
 * <ul>
 * <li>List all the files</li>
 * <li>List changes around them</li>
 * </ul>
 */
public class DriveExplorer extends SourceExplorer {

	// TODO: Do it

	public DriveExplorer(long sourceId) {
		super(sourceId);
	}

	@Override
	protected void explorerRun(EntityManager em) {
		SourceDrive srcDrive = (SourceDrive) source;
		int c = 0;
		int ok = 0;
		for (File f : srcDrive.getFilesLister()) {
			try {
				LOG.info("F {}: {} : {}", c++, f.getPath(), f.getName());
				ok++;
			} catch (AccessException ex) {
				LOG.error("Access", ex);
			}
		}
	}

}
