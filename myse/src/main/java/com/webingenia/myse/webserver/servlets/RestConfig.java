package com.webingenia.myse.webserver.servlets;

import com.google.gson.Gson;
import com.webingenia.myse.db.model.Config;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestConfig extends HttpServlet {

	private final Gson gson = new Gson();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String name = req.getParameter("name");
		String value = req.getParameter("value");

		if (value != null) {
			Config.set(name, value);
		}

		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(Config.get(name, (String) null)));
			}
		}

	}

}
