package com.webingenia.myse.access.ftps;

import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.Source;
import static com.webingenia.myse.access.vfs.SourceVFS.PROP_HOST;
import static com.webingenia.myse.access.vfs.SourceVFS.PROP_PATH;
import static com.webingenia.myse.access.vfs.SourceVFS.PROP_SCHEME;
import static com.webingenia.myse.access.vfs.SourceVFS.PROP_USER;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.model.DBDescSource;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

/**
 * FTPS source implementation.
 *
 * The VFS's one doesn't seem to be functionnal for directory listing.
 */
public class SourceFTPS extends Source {

	public static final String TYPE_IMPLICIT = "ftps";
	public static final String TYPE_EXPLICIT = "fpes";

	public static final String PROP_USER = "user",
			PROP_PASS = "pass",
			PROP_HOST = "host",
			PROP_DIR = "dir";

	FTPSClient client;
	private final boolean implicit;
	private final String host, user, pass;
	FTPClientConfig config;

	public SourceFTPS(DBDescSource desc) {
		super(desc);
		implicit = desc.getType().equals(TYPE_IMPLICIT);
		Map<String, String> props = desc.getProperties();
		host = props.get(PROP_HOST);
		user = props.get(PROP_USER);
		pass = props.get(PROP_PASS);
	}

	@Override
	public FileFTPS getRootDir() throws AccessException {
		return getFile("/");
	}

	public FTPSClient getClient() throws AccessException {
		if (client == null) {
			try {
				//int reply;
				client = new FTPSClient(implicit);
				client.setControlEncoding("UTF8");
				client.setCharset(Charset.forName("utf8"));
				client.connect(host);
				LOG.info("Connected to " + host + ".");
				checkReply();

				client.login(user, pass);
				checkReply();

				client.sendCommand("OPTS UTF8 ON");
				checkReply();

				client.execPBSZ(0);
				checkReply();

				client.execPROT("P");
				checkReply();

				client.enterLocalPassiveMode();
				checkReply();
			} catch (IOException ex) {
				throw new AccessException(AccessException.AccessState.ERROR, ex);
			}
		}
		return client;
	}

	private void checkReply() throws AccessException {
		LOG.info("[FTPS] --> " + client.getReplyString());
		if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
			throw new AccessException(AccessException.AccessState.DENIED, null);
		}
	}

	@Override
	public FileFTPS getFile(String path) throws AccessException {
		return new FileFTPS(path, this);
	}

	AccessException convertException(Exception ex) {
		return new AccessException(AccessException.AccessState.UNKNOWN, ex);
	}

	@Override
	public PropertyDescription[] getProperties() {
		return new PropertyDescription[]{
			new PropertyDescription(PROP_HOST, PropertyDescription.Type.TEXT, "Host"),
			new PropertyDescription(PROP_USER, PropertyDescription.Type.TEXT, "Username"),
			new PropertyDescription(PROP_USER, PropertyDescription.Type.PASSWORD, "Password"),
			new PropertyDescription(PROP_PATH, PropertyDescription.Type.TEXT, "Path of the directory to index")
		};
	}
}
