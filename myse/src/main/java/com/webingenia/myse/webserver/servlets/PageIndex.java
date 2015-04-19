package com.webingenia.myse.webserver.servlets;

import static com.webingenia.myse.common.LOG.LOG;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PageIndex extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			resp.sendRedirect("/static/index.html");
		} catch (Exception ex) {
			LOG.error("PageIndex error (" + req.getPathInfo() + ")", ex);
		}
	}
}
