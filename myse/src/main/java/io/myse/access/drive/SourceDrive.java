package io.myse.access.drive;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.google.api.services.drive.model.FileList;
import io.myse.access.AccessException;
import io.myse.access.File;
import io.myse.access.Source;
import io.myse.access.SourceEditingContext;
import static io.myse.common.LOG.LOG;
import io.myse.db.model.Config;
import io.myse.db.model.DBDescSource;
import io.myse.exploration.SourceExplorer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Google drive source implementation. Fetches all the files from google drive.
 */
public class SourceDrive extends Source {

	public static final String TYPE = "gdrive",
			PROP_CODE = "code",
			PROP_PATH = "path",
			PROP_ACCESS_TOKEN = "access_token",
			PROP_REFRESH_TOKEN = "refresh_token";

	public static final String OAUTH_CLIENT_ID = "387222605329-8uivjucupiajrg4ed0v2flm6kquon46d.apps.googleusercontent.com",
			OAUTH_CLIENT_SECRET = "flyEB2wXpC1MdLWwip-pVt1h";

	private static String getOAuthRedirectUri(String remaining) {
		String hostname = Config.get("hostname", "", false);
		if (hostname == null || !hostname.startsWith("localhost:")) {
			return "urn:ietf:wg:oauth:2.0:oob";
		}
		return "http://" + Config.get("hostname", "", false) + "/oauth/" + remaining;
	}

	static final boolean DEBUG = true;

	public SourceDrive(DBDescSource desc) {
		super(desc);
	}

	@Override
	public File getRootDir() throws AccessException {
		try {
			About a = getAbout();
			return (a != null) ? new FileDrive(a.getRootFolderId(), true, this) : null;
		} catch (IOException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	private About about;

	private About getAbout() throws IOException {
		if (about == null) {
			Drive drive = getDrive();
			about = drive != null ? drive.about().get().execute() : null;
		}
		return about;
	}

	@Override
	public File getFile(String path) throws AccessException {
		if (path.equals("/")) {
			return getRootDir();
		}
		return new FileDrive(path, this);
	}

	@Override
	public List<PropertyDescription> getProperties() {
		ArrayList<PropertyDescription> list = new ArrayList<>();
		list.addAll(Arrays.asList(
				new PropertyDescription(PROP_CODE, PropertyDescription.Type.TEXT, "Google drive authorization code", null, "Will be filled after you save")
		//new PropertyDescription(PROP_PATH, PropertyDescription.Type.TEXT, "Path of the directory to index")
		));
		list.addAll(getSharedProperties());
		return list;
	}

	HttpTransport httpTransport = new NetHttpTransport();
	JsonFactory jsonFactory = new JacksonFactory();

	@Override
	public void postSave(SourceEditingContext context) {
		DBDescSource dbSource = getDesc();
		if (Strings.isNullOrEmpty(getCode())) {
			context.ok = false;
			context.nextUrl = getAuthCodeFlow().newAuthorizationUrl().setRedirectUri(getOAuthRedirectUri("?source_id=" + dbSource.getId())).build();
		}
	}

	GoogleAuthorizationCodeFlow codeFlow;

	private GoogleAuthorizationCodeFlow getAuthCodeFlow() {
		if (codeFlow == null) {
			codeFlow = new GoogleAuthorizationCodeFlow.Builder(
					httpTransport, jsonFactory, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
					.setAccessType("offline")
					.build();
		}
		return codeFlow;
	}

	private String code;

	private String getCode() {
		if (code == null) {
			code = getDesc().getProperties().get(PROP_CODE);
		}
		return code;
	}

	private Drive driveService;

	Drive getDrive() throws IOException {
		return getDrive(0);
	}

	Drive getDrive(int attempt) throws IOException {
		if (attempt > 3) {
			return null;
		}
		if (driveService == null) {
			DBDescSource dbSource = getDesc();

			Map<String, String> props = dbSource.getProperties();

			if (props.containsKey(PROP_CODE)) {
				GoogleCredential credential;
				if (!props.containsKey(PROP_ACCESS_TOKEN)) {
					try {
						GoogleTokenResponse response = getAuthCodeFlow().newTokenRequest(getCode()).setRedirectUri(getOAuthRedirectUri("?source_id=" + dbSource.getId())).execute();
						props.put(PROP_ACCESS_TOKEN, response.getAccessToken());
						props.put(PROP_REFRESH_TOKEN, response.getRefreshToken());
					} catch (Exception ex) {
						LOG.warn("Issue while fetching drive credentials", ex);
						props.remove(PROP_CODE);
						return getDrive(attempt + 1);
					}
				}
				credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory).setTransport(httpTransport).setClientSecrets(OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET).build();
				credential.setAccessToken(props.get(PROP_ACCESS_TOKEN));
				credential.setRefreshToken(props.get(PROP_REFRESH_TOKEN));
				Drive drive = new Drive.Builder(httpTransport, jsonFactory, credential).build();
				try {
					about = drive.about().get().execute();
				} catch (GoogleJsonResponseException ex) {
					props.remove(PROP_ACCESS_TOKEN);
					return getDrive(attempt + 1);
				}
				driveService = drive;
			}
		}
		return driveService;
	}

	private final SourceDrive self = this;

	public class FilesLister implements Iterator<File> {

		private String pageToken;

		private boolean lastFetch;

		private LinkedList<File> files = new LinkedList<>();

		private void fetch() throws IOException {
			if (lastFetch) {
				files = null;
				return;
			}
			FileList list = getDrive().files().list().setPageToken(pageToken).execute();
			pageToken = list.getNextPageToken();
			List<com.google.api.services.drive.model.File> items = list.getItems();
			if (items.isEmpty()) {
				files = null;
			} else {
				for (com.google.api.services.drive.model.File f : items) {
					files.add(new FileDrive(f, self));
				}
			}
			if (pageToken == null) {
				lastFetch = true;
			}
		}

		@Override
		public boolean hasNext() {
			try {
				if (files.isEmpty()) {
					fetch();
				}
			} catch (Exception ex) {
				LOG.error("FilesLister", ex);
			}
			return files != null;
		}

		@Override
		public File next() {
			return files.poll();
		}

	}

	public Iterable<File> getFilesLister() {
		return new Iterable<File>() {

			@Override
			public Iterator<File> iterator() {
				return new FilesLister();
			}
		};
	}

	@Override
	public SourceExplorer getExplorer() {
		return new DriveExplorer(getDesc().getId());
	}
}
