package io.myse.access.dbox;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxDelta;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import io.myse.access.AccessException;
import io.myse.access.File;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.DBDescFile;
import io.myse.exploration.SourceExplorer;
import javax.persistence.EntityManager;

/**
 * Dropbox optimized explorer. It is more suitable than the default directory
 * analyser, it can fetch only the changes instead of scanning all the
 * directories.
 */
public class DBoxExplorer extends SourceExplorer {

	public DBoxExplorer(long sourceId) {
		super(sourceId);
	}

	private static final String PROP_DELTA_CURSOR = "delta_cursor";

	@Override
	protected void explorerRun(EntityManager em) throws Exception {
		SourceDBox src = ((SourceDBox) source);
		DbxClient client = src.getClient();
		try {
			String cursor = src.getDesc().getProperties().get(PROP_DELTA_CURSOR);
			DbxDelta<DbxEntry> delta = client.getDeltaWithPathPrefix(cursor, "/");
			if (!delta.entries.isEmpty()) {
				LOG.info("{}: Some changes...", this);
				for (DbxDelta.Entry<DbxEntry> e : delta.entries) {
					if (e.metadata != null) {
						// If the file exists
						File file = new FileDBox(e.metadata, src);
						LOG.info("{}: {} changed", this, file);
						indexFile(file, em);
					} else {
						LOG.info("{}: {} deleted", this, e.lcPath);
						// If the file has been deleted
						DBDescFile dbFile = DBDescFile.get(e.lcPath, em);
						if (dbFile != null) {
							// TODO: Delete the document as well
							em.remove(dbFile);
						}
					}
				}
				cursor = delta.cursor;
				src.getDesc().getProperties().put(PROP_DELTA_CURSOR, cursor);
				if (delta.hasMore) {
					delay -= 60000;
				}
			} else {
				delay += 15000;
				LOG.info("{}: No changes", this);
			}
		} catch (DbxException ex) {
			delay += 60000;
			throw new AccessException(AccessException.AccessState.ERROR, ex);
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

	@Override
	public String toString() {
		return String.format("DBoxExplorer{%s}", source);
	}
}
