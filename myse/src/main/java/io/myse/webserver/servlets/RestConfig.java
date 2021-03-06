package io.myse.webserver.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.myse.common.Passwords;
import io.myse.db.SettingsExporter;
import io.myse.db.model.Config;
import io.myse.exploration.DirExplorer;
import io.myse.webserver.JettyServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestConfig extends HttpServlet {

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static class ConfigDescription {

		public ConfigDescription(String name, Type type, String description) {
			this(name, type, description, Config.get(name, null, false));
		}

		public ConfigDescription(String name, Type type, String description, String value) {
			this.name = name;
			this.type = type;
			this.description = description;
			this.value = value;
		}

		public enum Type {

			TEXT,
			PASSWORD,
			BOOLEAN
		}
		public final String name;
		public final Type type;
		public final String description;
		public final String value;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (!Session.get(req).checkIsAdmin(resp)) {
			return;
		}
		String path = req.getPathInfo();
		if (path == null) {
			path = "";
		}
		Object output;
		switch (path) {
			case "/list":
				List<ConfigDescription> list = Arrays.asList(
						new ConfigDescription(Config.PAR_UPDATE_CHANNEL, ConfigDescription.Type.TEXT, "Update channel"),
						new ConfigDescription(DirExplorer.CONF_LOG_NEW_DIRS, ConfigDescription.Type.BOOLEAN, "Log new dirs"),
						new ConfigDescription(JettyServer.PROP_PORT, ConfigDescription.Type.TEXT, "Web server port"),
						new ConfigDescription(Config.PAR_HOSTNAME, ConfigDescription.Type.TEXT, "Server hostname"),
						new ConfigDescription(Config.PAR_ALLOW_DOWNLOAD, ConfigDescription.Type.BOOLEAN, "Allow download"),
						new ConfigDescription(Config.PAR_ALLOW_LINK, ConfigDescription.Type.BOOLEAN, "Allow link"),
						new ConfigDescription(Config.PAR_ALLOW_GUEST_LOGIN, ConfigDescription.Type.BOOLEAN, "Allow guests to login"),
						new ConfigDescription(Config.PAR_ALLOW_GUEST_ADMIN, ConfigDescription.Type.BOOLEAN, "Allows guests as admin"),
						new ConfigDescription(Passwords.CONFIG_SALT, ConfigDescription.Type.TEXT, "Passwords salt")
				);
				output = list;
				break;

			default: {
				String name = req.getParameter("name");
				String value = req.getParameter("value");

				if (value != null) {
					Config.set(name, value);
					SettingsExporter.autoSave();
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
