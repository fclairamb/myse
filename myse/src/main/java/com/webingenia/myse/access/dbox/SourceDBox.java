package com.webingenia.myse.access.dbox;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.webingenia.myse.access.AccessException;
import com.webingenia.myse.access.File;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.access.SourceEditingContext;
import com.webingenia.myse.common.BuildInfo;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.model.DBDescSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Dropbox source implementation.
 */
public class SourceDBox extends Source {
	// TODO: Implement authorization code feedback (there's an issue with sessions at this stage)
	// TODO: Implement optimized explorer

	public static final String TYPE = "dbox";

	private static final String APP_KEY = "uu0ccytonysxlto",
			APP_SECRET = "prb1xmppnwbup91",
			OAUTH_REDIRECT_URI = "http://localhost:10080/oauth/",
			PROP_CODE = "code",
			PROP_ACCESS_TOKEN = "access_token";

	DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
	DbxRequestConfig config = new DbxRequestConfig(
			"MySE/" + BuildInfo.VERSION, Locale.getDefault().toString());

	public SourceDBox(DBDescSource desc) {
		super(desc);
	}

	@Override
	public File getRootDir() throws AccessException {
		return getFile("/");
	}

	@Override
	public File getFile(String path) throws AccessException {
		return new FileDBox(path, this);
	}

	@Override
	public List<PropertyDescription> getProperties() {
		ArrayList<PropertyDescription> list = new ArrayList<>();
		list.addAll(Arrays.asList(
				new PropertyDescription(PROP_CODE, PropertyDescription.Type.TEXT, "Dropbox authorization code", null, "Will be filled after you save")
		));
		list.addAll(getSharedProperties());
		return list;
	}

	private DbxClient client;

	public DbxClient getClient() {
		if (client == null) {
			String accessToken = getDesc().getProperties().get(PROP_ACCESS_TOKEN);
			if (accessToken != null) {
				client = new DbxClient(config, accessToken);
			}
		}
		return client;
	}

	@Override
	public void postSave(SourceEditingContext context) {
		Map<String, String> properties = getDesc().getProperties();
		DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
		if (!properties.containsKey(PROP_CODE)) {
			//HttpSession httpSession = new MemSession(null, System.currentTimeMillis(), System.currentTimeMillis(), null);
//			DbxSessionStore store = new DbxStandardSessionStore(context.httpSession, "key");
			//DbxWebAuth webAuth = new DbxWebAuth(config, appInfo, OAUTH_REDIRECT_URI + "?source_id=" + getDesc().getId(), null);

			context.nextUrl = webAuth.start();
		} else if (!properties.containsKey(PROP_ACCESS_TOKEN)) {
			try {
				String code = properties.get(PROP_CODE);
				DbxAuthFinish finish = webAuth.finish(code);
				properties.put(PROP_ACCESS_TOKEN, finish.accessToken);
			} catch (DbxException ex) {
				properties.remove(PROP_CODE);
				LOG.error("Dropbox exception", ex);
			}
		}
	}
}
