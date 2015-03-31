package com.webingenia.myse.webserver.servlets;

import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Florent Clairambault
 */
public class Headers {

	public static void setCacheDuration(HttpServletResponse response, long time) {
		if (time == -1) {
			time = 60 * 60 * 24 * 7;
		}
		long timeMs = time * 1000;
		long now = System.currentTimeMillis();
		response.addHeader("Cache-Control", "PUBLIC, max-age=" + time);
		response.setDateHeader("Last-Modified", now);
		response.setDateHeader("Expires", now + timeMs);
	}

	public static void setCacheExpiration(HttpServletResponse response, long expiration) {
		long now = System.currentTimeMillis();
		response.addHeader("Cache-Control", "PUBLIC, max-age=" + ((expiration - now) / 1000));
		response.setDateHeader("Last-Modified", now);
		response.setDateHeader("Expires", expiration);
	}
}
