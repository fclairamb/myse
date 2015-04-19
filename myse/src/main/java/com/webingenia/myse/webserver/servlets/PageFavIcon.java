package com.webingenia.myse.webserver.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.elasticsearch.common.io.Streams;

public class PageFavIcon extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("image/png");
		try (OutputStream os = resp.getOutputStream()) {
			URL resource = getClass().getResource("/icons/icon_032.png");
			try (InputStream is = resource.openStream()) {
				Streams.copy(is, os);
			}
		}
	}

}
