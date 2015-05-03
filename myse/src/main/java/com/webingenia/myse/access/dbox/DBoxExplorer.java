package com.webingenia.myse.access.dbox;

import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.util.Maybe;
import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.exploration.SourceExplorer;
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

	private static final String PROP_CHANGES_HASH = "hash_changes";

	@Override
	protected void explorerRun(EntityManager em) throws Exception {
		SourceDBox src = ((SourceDBox) source);
		DbxClient client = src.getClient();
		try {
			String hash = src.getDesc().getProperties().get(PROP_CHANGES_HASH);
			Maybe<DbxEntry.WithChildren> metadataWithChildren = client.getMetadataWithChildrenIfChanged("/", hash);
			if (metadataWithChildren.isJust()) {
				for (DbxEntry e : metadataWithChildren.getJust().children) {
					analyseFile(new FileDBox(e, src));
				}
			}
			src.getDesc().getProperties().put(PROP_CHANGES_HASH, hash);
		} catch (DbxException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	private void analyseFile(FileDBox fileDBox) {
		// TODO: Have some generic file handling code in SourceExplorer
	}

}
