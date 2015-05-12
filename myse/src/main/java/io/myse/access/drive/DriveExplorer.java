package io.myse.access.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import io.myse.access.AccessException;
import io.myse.access.File;
import static io.myse.common.LOG.LOG;
import io.myse.exploration.SourceExplorer;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

/**
 * Google Drive explorer. It should:
 * <ul>
 * <li>List all the files</li>
 * <li>List changes around them</li>
 * </ul>
 */
public class DriveExplorer extends SourceExplorer {

	public DriveExplorer(long sourceId) {
		super(sourceId);
	}

	private static final String PROP_LAST_CHANGE_ID = "last_change_id";

	private Iterator<File> filesIterator;
	private long largestChangeId;
	private long nbFiles;

	private boolean indexEverything(SourceDrive srcDrive, EntityManager em) throws AccessException, IOException {

		LOG.info("{}: Indexing everything...", this);
		if (filesIterator == null) {
			largestChangeId = srcDrive.getDrive().changes().list().execute().getLargestChangeId();
			filesIterator = srcDrive.getFilesLister().iterator();
		}

		int c;

		// Then we fetch all the files
		for (c = 0; c < 50 && filesIterator.hasNext(); c++) {
			File f = filesIterator.next();
			LOG.info("{}: {}, count={}, id={}", this, f.getName(), ++nbFiles, f.getPath());
			indexFile(f, em);
		}

		// We save the largest change id only once we don't have anything more to index
		if (c == 0) {
			Map<String, String> properties = srcDrive.getDesc().getProperties();
			properties.put(PROP_LAST_CHANGE_ID, Long.toString(largestChangeId));
			return true;
		}

		return false;
	}

	@Override
	protected void explorerRun(EntityManager em) throws AccessException {
		try {
			SourceDrive srcDrive = (SourceDrive) source;
			
			if ( ! source.getDesc().doIndex() ) {
				if ( source.getDesc().deleted() ) {
					cancel();
				}
				return;
			}
			
			Map<String, String> properties = srcDrive.getDesc().getProperties();

			// There's actually no point in having a pageToken if we can fetch
			// changes using the change number.
			Drive.Changes.List request = srcDrive.getDrive().changes().list();

			long lastChangeId = 0;

			{
				String sLastChangeId = properties.get(PROP_LAST_CHANGE_ID);
				if (sLastChangeId != null) {
					lastChangeId = Long.parseLong(sLastChangeId);
					request.setStartChangeId(lastChangeId);
				} else {
					// At this stage this solution is worse than changes parsing
					// because we lock the table for too much time
					if (!indexEverything(srcDrive, em)) { // If we still have things to index...
						delay = 100;
					}
					return; // We have to return now anyway, to re-read the lastChangeId
				}
			}

			LOG.info("{}: Fetching changes from lastChangeId={}", this, lastChangeId);
			ChangeList changeList = request.execute();
			List<Change> items = changeList.getItems();
			if (items.isEmpty()) {
				delay = 30000;
				return;
			} else {
				delay = 100;
			}
			for (Change chg : items) {
				FileDrive fileDrive;
				if (chg.getDeleted()) {
					fileDrive = new FileDrive(chg.getFileId(), srcDrive);
				} else {
					fileDrive = new FileDrive(chg.getFile(), srcDrive);
				}
				indexFile(fileDrive, em);
				LOG.info("{}: Indexing {} (change {})", this, fileDrive, chg.getId());
				lastChangeId = chg.getId() + 1;
			}

//				pageToken = changeList.getNextPageToken();
			properties.put(PROP_LAST_CHANGE_ID, Long.toString(lastChangeId));
		} catch (IOException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	@Override
	public String toString() {
		return String.format("DriveExplorer{%s}", source);
	}

}
