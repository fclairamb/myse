package com.webingenia.myse.webserver;

import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.webserver.servlets.PageIndex;
import com.webingenia.myse.webserver.servlets.PageStatic;
import com.webingenia.myse.webserver.servlets.RestSearch;
import com.webingenia.myse.webserver.servlets.RestSetupSource;
import com.webingenia.myse.webserver.servlets.RestVersion;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class JettyServer {

	private static Server server;
	private static ServletHandler handler;

	public static void start() throws Exception {
		server = new Server(Config.get("jetty_port", 8080));

		loadHandler();

		server.start();
	}

	public static void stop() throws Exception {
		server.stop();
		server.join();
	}

	private static void loadHandler() {
		handler = new ServletHandler();
		handler.addServletWithMapping(PageIndex.class, "/");
		handler.addServletWithMapping(PageStatic.class, "/static/*");
		handler.addServletWithMapping(RestSearch.class, "/rest/search");
		handler.addServletWithMapping(RestSetupSource.class, "/rest/setup/source");
		handler.addServletWithMapping(RestVersion.class, "/rest/version");
		server.setHandler(handler);
	}
}
