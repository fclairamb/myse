package io.myse.webserver;

import io.myse.db.model.Config;
import io.myse.webserver.servlets.PageDownload;
import io.myse.webserver.servlets.PageFavIcon;
import io.myse.webserver.servlets.PageIndex;
import io.myse.webserver.servlets.PageOAuth;
import io.myse.webserver.servlets.RestConfig;
import io.myse.webserver.servlets.PageStatic;
import io.myse.webserver.servlets.RestQuit;
import io.myse.webserver.servlets.RestSearch;
import io.myse.webserver.servlets.RestSetupSource;
import io.myse.webserver.servlets.RestStats;
import io.myse.webserver.servlets.RestVersion;
import io.myse.webserver.servlets.WSAdapter;
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
