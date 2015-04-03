package com.webingenia.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.webingenia.myse.common.BuildInfo;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestVersion extends HttpServlet {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static class Info {

		public String general;
		public String buildId;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Info info = new Info();
		info.general = BuildInfo.BUILD_SIMPLIFIED_INFO;
		info.buildId = BuildInfo.BUILD_ID;
		
		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(info));
			}
		}
	}

}
