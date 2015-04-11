package com.webingenia.myse.webserver.servlets;

import com.google.gson.Gson;
import com.webingenia.myse.access.Source;
import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.direxplore.DirExplorer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestConfig extends HttpServlet {

	private final Gson gson = new Gson();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String path = req.getPathInfo();
		Object output = null;
		switch (path) {
			case "/list":
				List<Source.PropertyDescription> list = Arrays.asList(
						new Source.PropertyDescription(Config.PAR_UPDATE_CHANNEL, Source.PropertyDescription.Type.TEXT, "Update channel"),
						new Source.PropertyDescription(DirExplorer.CONF_LOG_NEW_DIRS, Source.PropertyDescription.Type.BOOLEAN, "Log new dirs")
				);
				output = list;
				break;

			default: {
				String name = req.getParameter("name");
				String value = req.getParameter("value");

				if (value != null) {
					Config.set(name, value);
				}

				output = Config.get(name, (String) null, false);
			}
		}

		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(output));
			}
		}
	}

}
