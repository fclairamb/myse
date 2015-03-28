package com.webingenia.myse.access.smb;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.model.DBDescSource;
import java.net.MalformedURLException;
import java.util.Map;
import jcifs.smb.SmbFile;

public class SourceSMB extends Source {

	public static final String TYPE = "smb";

	public static final String PROP_USER = "user",
			PROP_PASS = "pass",
			PROP_HOST = "host",
			PROP_PATH = "path";

	public SourceSMB(DBDescSource desc) {
		super(desc);
	}

	private String getRootPath() {
		Map<String, String> props = desc.getProperties();
		String user = props.get(PROP_USER),
				pass = props.get(PROP_PASS),
				host = props.get(PROP_HOST),
				dir = props.get(PROP_PATH);
		return String.format("smb://%s:%s@%s/%s/", user, pass, host, dir);
	}

	@Override
	public FileSMB getRootDir() {
		try {

			SmbFile root = new SmbFile(getRootPath());
			int length = root.getPath().length();
			return new FileSMB(root, this);
		} catch (MalformedURLException ex) {
			LOG.error("Could not create SMB root dir", ex);
			return null;
		}
	}

	private int pathOffset;

	int getPathOffset() {
		if (pathOffset == 0) {
			pathOffset = getRootDir().getFile().getPath().length();
		}
		return pathOffset;
	}

	@Override
	public File getFile(String path) throws AccessException {
		try {
			return new FileSMB(new SmbFile(getRootPath() + path), this);
		} catch (MalformedURLException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}
}