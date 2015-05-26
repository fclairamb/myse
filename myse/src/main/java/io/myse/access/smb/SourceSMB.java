package io.myse.access.smb;

import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.DBDescSource;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
		return String.format("smb://%s:%s@%s/%s", user, pass, host, dir);
	}

	@Override
	public FileSMB getRootDir() {
		try {

			SmbFile root = new SmbFile(getRootPath());
			//int length = root.getPath().length();
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

	@Override
	public List<PropertyDescription> getProperties() {
		ArrayList<PropertyDescription> list = new ArrayList<>();
		list.addAll(Arrays.asList(
				new PropertyDescription(PROP_HOST, PropertyDescription.Type.TEXT, "Host", null, "192.168.1.1"),
				new PropertyDescription(PROP_USER, PropertyDescription.Type.TEXT, "Username", null, "my user"),
				new PropertyDescription(PROP_PASS, PropertyDescription.Type.PASSWORD, "Password", null, "my password"),
				new PropertyDescription(PROP_PATH, PropertyDescription.Type.TEXT, "Path of the directory to index", null, "Shared folder")
		));
		list.addAll(getSharedProperties());
		return list;
	}
}
