package io.myse.access.web;

import io.myse.access.AccessException;
import io.myse.access.web.FileWeb.LinkContent;
import io.myse.common.BuildInfo;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class FetchCacher {

	// Guava cache would be better, but I don't want to add an other dependency. 
	// This will be enough for now.
	private static final Map<String, FileWeb.LinkContent> cache = Collections.synchronizedMap(new Cache());

	public static class Cache extends LinkedHashMap<String, FileWeb.LinkContent> {

		private static final int MAX_ENTRIES = 500;

		@Override
		protected boolean removeEldestEntry(Entry<String, FileWeb.LinkContent> eldest) {
			return size() > MAX_ENTRIES;
		}
	}

	public static FileWeb.LinkContent get(String path) throws AccessException {
		FileWeb.LinkContent content = cache.get(path);

		if (content == null) {
			content = fetchLinkContent(path);
			cache.put(path, content);
		}

		return content;
	}

	private static LinkContent fetchLinkContent(String path) throws AccessException {
		try {
			LinkContent newContent = new LinkContent();
			URL url = new URL(path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("User-Agent", String.format("MySE/%s (http://www.myse.io)", BuildInfo.VERSION));

			// The input stream is NOT closed. The GC will take care of it. It's 
			// because it can be later used to continue streaming the
			// BufferedInputStream.
			InputStream is = connection.getInputStream();
			//try (InputStream is = connection.getInputStream()) {
			newContent.returnCode = connection.getResponseCode();
			newContent.size = connection.getContentLength();
			newContent.lastModified = new Date(connection.getLastModified() != 0 ? connection.getLastModified() : System.currentTimeMillis());
			newContent.setInputStream( new BufferedInputStream(is) );
			return newContent;
			//}
		} catch (Exception ex) {
			throw new AccessException(AccessException.AccessState.ERROR, ex);
		}
	}

}
