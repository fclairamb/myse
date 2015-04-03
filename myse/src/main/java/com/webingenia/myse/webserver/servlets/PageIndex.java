package com.webingenia.myse.webserver.servlets;

import static com.webingenia.myse.common.LOG.LOG;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.common.io.Streams;

public class PageIndex extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//resp.setContentType("text/html");
		try {
			//resp.setHeader("Location", );
			resp.sendRedirect("/static/index.html");
			try (OutputStream os = resp.getOutputStream()) {
				try (InputStream is = getClass().getResource("/web_static/index.html").openStream()) {
					Streams.copy(is, os);
				}
			}
		} catch (Exception ex) {
			LOG.error("PageIndex error", ex);
		}
	}
}
