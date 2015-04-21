package com.webingenia.myse.access.drive;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.About;
import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.db.DBMgmt;
import com.webingenia.myse.db.model.DBDescSource;
import com.webingenia.myse.desktop.Browser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;

public class SourceDrive extends Source {

	public static final String TYPE = "gdrive",
			PROP_CODE = "code",
			PROP_PATH = "path",
			PROP_ACCESS_TOKEN = "access_token",
			PROP_REFRESH_TOKEN = "refresh_token";

	public static final String OAUTH_CLIENT_ID = "387222605329-591p43omt8atr1lcmfm65kgeaaaerq2e.apps.googleusercontent.com",
			OAUTH_CLIENT_SECRET = "xovxNSNQDHKMXeZHJST8k3fk",
			OAUTH_REDIRECT_URI = "http://localhost:10080/oauth/";

	public SourceDrive(DBDescSource desc) {
		super(desc);
	}

	@Override
	public File getRootDir() throws AccessException {
		try {
			return new FileDrive("D:" + getAbout().getRootFolderId(), this);
		} catch (IOException ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

	private About about;

	private About getAbout() throws IOException {
		if (about == null) {
			about = getDrive().about().get().execute();
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
				new PropertyDescription(PROP_CODE, PropertyDescription.Type.TEXT, "Google drive authorization code")
		//new PropertyDescription(PROP_PATH, PropertyDescription.Type.TEXT, "Path of the directory to index")
		));
		list.addAll(getSharedProperties());
		return list;
	}

	HttpTransport httpTransport = new NetHttpTransport();
	JsonFactory jsonFactory = new JacksonFactory();

	@Override
	public void postSave() {
		DBDescSource dbSource = getDesc();
		String url = getAuthCodeFlow().newAuthorizationUrl().setRedirectUri(OAUTH_REDIRECT_URI + "?source_id=" + dbSource.getId()).build();
		Browser.show(url);
	}

	GoogleAuthorizationCodeFlow codeFlow;

	private GoogleAuthorizationCodeFlow getAuthCodeFlow() {
		if (codeFlow == null) {
			codeFlow = new GoogleAuthorizationCodeFlow.Builder(
					httpTransport, jsonFactory, OAUTH_CLIENT_ID, OAUTH_CLIENT_SECRET, Arrays.asList(DriveScopes.DRIVE))
					.setAccessType("online")
					.setApprovalPrompt("auto").build();
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
		if (driveService == null) {
			EntityManager em = DBMgmt.getEntityManager();
			try {
				em.getTransaction().begin();
				DBDescSource dbSource = getDesc();
				GoogleCredential credential;
				Map<String, String> props = dbSource.getProperties();
				if (!props.containsKey(PROP_ACCESS_TOKEN)) {
					GoogleTokenResponse response = getAuthCodeFlow().newTokenRequest(getCode()).setRedirectUri(OAUTH_REDIRECT_URI + "?source_id=" + dbSource.getId()).execute();
					credential = new GoogleCredential().setFromTokenResponse(response);
					String accessToken = credential.getAccessToken();
					props.put(PROP_ACCESS_TOKEN, accessToken);
					String refreshToken = credential.getRefreshToken();
					if (refreshToken != null) {
						props.put(PROP_REFRESH_TOKEN, refreshToken);
					} else {
						props.remove(PROP_REFRESH_TOKEN);
					}
					em.persist(dbSource);
				} else {
					credential = new GoogleCredential().setAccessToken(props.get(PROP_ACCESS_TOKEN)).setRefreshToken(props.get(PROP_REFRESH_TOKEN));
				}
				driveService = new Drive.Builder(httpTransport, jsonFactory, credential).build();
			} finally {
				em.getTransaction().commit();
				em.close();
			}
		}
		return driveService;
	}

}
