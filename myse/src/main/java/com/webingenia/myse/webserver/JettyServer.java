package com.webingenia.myse.webserver;

import com.webingenia.myse.common.LOG;
import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.webserver.servlets.PageDownload;
import com.webingenia.myse.webserver.servlets.PageFavIcon;
import com.webingenia.myse.webserver.servlets.PageIndex;
import com.webingenia.myse.webserver.servlets.PageOAuth;
import com.webingenia.myse.webserver.servlets.RestConfig;
import com.webingenia.myse.webserver.servlets.PageStatic;
import com.webingenia.myse.webserver.servlets.RestQuit;
import com.webingenia.myse.webserver.servlets.RestSearch;
import com.webingenia.myse.webserver.servlets.RestSetupSource;
import com.webingenia.myse.webserver.servlets.RestStats;
import com.webingenia.myse.webserver.servlets.RestVersion;
import com.webingenia.myse.webserver.servlets.WSAdapter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyServer {

	private static Server server;
	private static ServletHandler handler;

	private static final String PRE = "web_server.";

	public static final String PROP_PORT = PRE + "port";

	public static void start() throws Exception {
		if (server == null) {
			server = new Server(Config.get(PROP_PORT, 10080, true));
			loadHandler();
			server.start();
		}
	}

	public static void stop() throws Exception {
		if (server != null) {
			server.stop();
			server.join();
			server = null;
		}
	}

	private static void loadHandler() {
		handler = new ServletHandler();
		handler.addServletWithMapping(PageIndex.class, "/");
		handler.addServletWithMapping(PageStatic.class, "/static/*");
		handler.addServletWithMapping(PageFavIcon.class, "/favicon.ico");
		handler.addServletWithMapping(PageDownload.class, "/download");

		handler.addServletWithMapping(RestSearch.class, "/rest/search");
		handler.addServletWithMapping(RestSetupSource.class, "/rest/setup/source/*");
		handler.addServletWithMapping(RestConfig.class, "/rest/setup/config/*");
		handler.addServletWithMapping(RestVersion.class, "/rest/version");
		handler.addServletWithMapping(RestStats.class, "/rest/stats");
		handler.addServletWithMapping(RestQuit.class, "/rest/quit");
		handler.addServletWithMapping(PageOAuth.class, "/oauth/*");

		// Add a websocket to a specific path spec
		ServletHolder holderEvents = new ServletHolder("ws-events", WSServlet.class);
		handler.addServletWithMapping(holderEvents, "/ws");

		server.setHandler(handler);
	}

	public static class WSServlet extends WebSocketServlet {

		@Override
		public void configure(WebSocketServletFactory factory) {
			factory.register(WSAdapter.class);
		}

	}
}
