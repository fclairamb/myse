package com.webingenia.myse.access.vfs;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.Source;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.model.DBDescSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.ftps.FtpsFileSystemConfigBuilder;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.elasticsearch.common.base.Strings;

public class SourceVFS extends Source {

	public static final String TYPE = "vfs";

	public static final String PROP_SCHEME = "scheme",
			PROP_HOST = "host",
			PROP_USER = "user",
			PROP_PASS = "pass",
			PROP_PORT = "port",
			PROP_PATH = "path";

	private StandardFileSystemManager manager;

	private final String fsUrl;
	private final FileSystemOptions fsOpts;

	private StandardFileSystemManager getManager() throws FileSystemException {
		if (manager == null) {
			manager = new StandardFileSystemManager();
			manager.init();
		}
		return manager;
	}

	public SourceVFS(DBDescSource desc) {
		super(desc);

		{ // We work on the FS options
			this.fsOpts = new FileSystemOptions();
			try {
				Map<String, String> props = getDesc().getProperties();
				String scheme = props.get(PROP_SCHEME);
				if (scheme != null) {
					switch (scheme) {
						case "sftp":
							SftpFileSystemConfigBuilder.getInstance().setTimeout(fsOpts, 10000);
							SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(fsOpts, "no");
							break;
						case "ftpes":
							props.put(PROP_SCHEME, "ftps");
							FtpsFileSystemConfigBuilder.getInstance().setFtpsType(fsOpts, "explicit");
							FtpsFileSystemConfigBuilder.getInstance().setPassiveMode(fsOpts, true);
							break;
					}
				}
			} catch (FileSystemException ex) {
				LOG.error("Error setting some FS options", ex);
			}
		}

		{ // We build the fsUrl
			Map<String, String> props = desc.getProperties();
			StringBuilder url = new StringBuilder();
			url.append(props.get(PROP_SCHEME)).append("://");

			{
				String user = props.get(PROP_USER);
				String pass = props.get(PROP_PASS);
				if (!Strings.isNullOrEmpty(user)) {
					url.append(":").append(pass).append("@");
				}
			}

			url.append(props.get(PROP_HOST));

			{
				String port = props.get(PROP_PORT);
				if (port != null) {
					url.append(":").append(port);
				}
			}

			url.append(props.get(PROP_PATH));

			this.fsUrl = url.toString();
		}
	}

	@Override
	public FileVFS getRootDir() throws AccessException {
		return getFile("");
	}

	@Override
	public FileVFS getFile(String path) throws AccessException {
		try {
			return new FileVFS(getManager().resolveFile(fsUrl + path, fsOpts), this);
		} catch (FileSystemException ex) {
			throw convertException(ex);
		}
	}

	AccessException convertException(FileSystemException ex) {
		return new AccessException(AccessException.AccessState.ERROR, ex);
	}

	private int pathOffset;

	int getPathOffset() {
		if (pathOffset == 0) {
			try {
				pathOffset = getRootDir().file.getURL().toString().length();
			} catch (Exception ex) {
				LOG.error("pathOffset", ex);
				pathOffset = -1;
			}
		}
		return pathOffset;
	}

	@Override
	public List<PropertyDescription> getProperties() {
		ArrayList<PropertyDescription> list = new ArrayList<>();
		list.addAll(Arrays.asList(
				new PropertyDescription(PROP_SCHEME, PropertyDescription.Type.TEXT, "VFS protocol scheme", null, "ftp / ftps / sftp / webdav"),
				new PropertyDescription(PROP_HOST, PropertyDescription.Type.TEXT, "Host", null, "192.168.1.1"),
				new PropertyDescription(PROP_USER, PropertyDescription.Type.TEXT, "Username", null, "myuser"),
				new PropertyDescription(PROP_PASS, PropertyDescription.Type.PASSWORD, "Password", null, "mypassword"),
				new PropertyDescription(PROP_PATH, PropertyDescription.Type.TEXT, "Path of the directory to index", null, "/")
		));
		list.addAll(getSharedProperties());
		return list;
	}

}
