package io.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.myse.common.BuildInfo;
import io.myse.db.model.Config;
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
		info.general = BuildInfo.VERSION;
		info.buildId = BuildInfo.NUMBER;

		{ // The response
			resp.setContentType("application/json; charset=utf8");
			try (PrintWriter out = resp.getWriter()) {
				out.write(gson.toJson(info));
			}
		}
		
		if ( Config.get("hostname", null, false) == null ){
			String host = req.getHeader("host");
			if ( host != null ) {
				Config.set("hostname", host);
			}
		}
	}

}
