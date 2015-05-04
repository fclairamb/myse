package io.myse.webserver.servlets;

import static io.myse.common.LOG.LOG;
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
import org.apache.tika.Tika;
import org.elasticsearch.common.io.Streams;

public class PageStatic extends HttpServlet {

//	private static final long EXPIRATION_DAYS_SHORT = 2L;
//	private static final long EXPIRATION_DAYS_LONG = 30L;
//	private static final long EXPIRATION_STANDARD = EXPIRATION_DAYS_LONG * 24 * 3600 * 1000; // A month
	private final Tika tika = new Tika();

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String path = request.getPathInfo();

			URL resource = null;
			File file;
			{ // DEV ONLY: We try to get the file directly
				file = new File("src/main/resources/web_static/" + path);
				if (file.exists()) {
					resource = file.toURI().toURL();
				}
			}
			if (resource == null) { // STANDARD OPERATION: The gile comes from the resources
				resource = getClass().getResource("/web_static" + path);
			}

			if (resource == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "File \"" + path + "\" not found !");
				try (PrintWriter out = response.getWriter()) {
					out.println("<h1>" + path + "NOT FOUND </h1>");
					out.println("<p>" + path + " not found !</p>");
				}
			} else {
				// DONE: Using file's name instead of content for type detection
				String contentType = tika.detect(path);
				response.setHeader("Content-Type", contentType);
				try (OutputStream os = response.getOutputStream()) {
					try (InputStream is = resource.openStream()) {
						Streams.copy(is, os);
					}
				}
			}
		} catch (Exception ex) {
			LOG.error("Static page", ex);
		}
	}
}
