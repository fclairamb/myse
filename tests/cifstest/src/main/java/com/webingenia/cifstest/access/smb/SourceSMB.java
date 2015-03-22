package com.webingenia.cifstest.access.smb;

import com.webingenia.cifstest.access.File;
import com.webingenia.cifstest.access.Source;
import static com.webingenia.cifstest.common.LOG.LOG;
import com.webingenia.cifstest.db.model.DBDescSource;
import java.net.MalformedURLException;
import java.util.Map;
import jcifs.smb.SmbFile;

public class SourceSMB extends Source {

	public static final String TYPE = "smb";

	public static final String PROP_USER = "user",
			PROP_PASS = "pass",
			PROP_HOST = "host",
			PROP_DIR = "dir";

	public SourceSMB(DBDescSource desc) {
		super(desc);
	}

	@Override
	public File getRootDir() {
		try {
			Map<String, String> props = desc.getProperties();
			String user = props.get(PROP_USER),
					pass = props.get(PROP_PASS),
					host = props.get(PROP_HOST),
					dir = props.get(PROP_DIR);
			SmbFile root = new SmbFile(String.format("smb://%s:%s@%s/%s/", user, pass, host, dir));
			int length = root.getPath().length();
			return new FileSMB(root, length);
		} catch (MalformedURLException ex) {
			LOG.error("Could not create SMB root dir", ex);
			return null;
		}
	}
}