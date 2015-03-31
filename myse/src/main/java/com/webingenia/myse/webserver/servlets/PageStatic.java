package com.webingenia.myse.webserver.servlets;

import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.common.Streams;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;

public class PageStatic extends HttpServlet {

	private static final long EXPIRATION_DAYS_SHORT = 2L;
	private static final long EXPIRATION_DAYS_LONG = 30L;
	private static final long EXPIRATION_STANDARD = EXPIRATION_DAYS_LONG * 24 * 3600 * 1000; // A month

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			long rangeStart, rangeStop;
			boolean doRange = false;

			String path = request.getPathInfo();

			URL resource = null;
			File file = null;
			{ // DEV ONLY: We try to get the file directly
				file = new File("src/main/resources/web_static/" + path);
				if (file.exists()) {
					resource = file.toURI().toURL();
				}
			}
			if (resource == null) { // STANDARD OPERATION: The gile comes from the resources
				resource = getClass().getResource("/web_static" + path);
				file = resource != null ? new File(resource.toURI()) : null;
			}

			if (file != null && file.isFile()) {
				long size = file.length();
				rangeStart = 0;
				rangeStop = size - 1;

				{ // We're reading the range header
					String range = request.getHeader("Range");
					if (range != null) {
						int p = range.indexOf('=');
						String type = range.substring(0, p);
						if ("bytes".equals(type)) {
							range = range.substring(p + 1);
							p = range.indexOf('/');
							if (p != -1) {
								String sLength = range.substring(p + 1);
								int length = Integer.parseInt(sLength);
								if (length != rangeStop) {
									response.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "We don't have the same length expectation !");
									return;
								}
								range = range.substring(0, p);
							}
							p = range.indexOf('-');
							String sRangeFrom = range.substring(0, p);
							String sRangeTo = range.substring(p + 1);
							if (!sRangeFrom.isEmpty()) {
								rangeStart = Integer.parseInt(sRangeFrom);
							}
							if (!sRangeTo.isEmpty()) {
								rangeStop = Integer.parseInt(sRangeTo);
							}
							doRange = true;
							response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
						}
					}
				}

				long length = (rangeStop - rangeStart) + 1;

				//MagicMatch mime = Magic.getMagicMatch(file, true, false);
				//response.setHeader("Content-Type", mime.getMimeType());
				String contentType = Files.probeContentType(new File(file.getAbsolutePath()).toPath());
				response.setHeader("Content-Type", contentType);
				response.setHeader("Content-Length", "" + length);
				if (doRange) {
					response.setHeader("Content-Range", "bytes " + rangeStart + "-" + rangeStop + "/" + size);
				}

				Headers.setCacheExpiration(response, System.currentTimeMillis() + EXPIRATION_STANDARD);

				try (OutputStream os = response.getOutputStream()) {
					try (InputStream is = resource.openStream()) {
						is.skip(rangeStart);
						Streams.copy(is, os, length);
					}
				}

			} else {
				if (file == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found !");
				} else {
					//response.sendError(HttpServletResponse.SC_FORBIDDEN, "Directory listing is not permitted !");
					try (PrintWriter out = response.getWriter()) {
						out.println("<h1>" + file.getAbsolutePath() + "</h1>");
						for (File f : file.listFiles()) {
							out.println("<a href=\"" + (path.endsWith("/") ? "" : "/static/" + path + "/") + f.getName() + "\">" + f.getName() + "</a><br />");
						}
					}
				}
			}
		} catch (Exception ex) {
			LOG.error("Static page", ex);
		}
	}
}
