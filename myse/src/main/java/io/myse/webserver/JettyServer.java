package io.myse.webserver;

import io.myse.db.model.Config;
import io.myse.webserver.servlets.PageDownload;
import io.myse.webserver.servlets.PageFavIcon;
import io.myse.webserver.servlets.PageIndex;
import io.myse.webserver.servlets.RestLink;
import io.myse.webserver.servlets.PageOAuth;
import io.myse.webserver.servlets.RestConfig;
import io.myse.webserver.servlets.PageStatic;
import io.myse.webserver.servlets.RestLogin;
import io.myse.webserver.servlets.RestLogout;
import io.myse.webserver.servlets.RestQuit;
import io.myse.webserver.servlets.RestSearch;
import io.myse.webserver.servlets.RestSetupSource;
import io.myse.webserver.servlets.RestStats;
import io.myse.webserver.servlets.RestSetupUser;
import io.myse.webserver.servlets.RestVersion;
import io.myse.webserver.servlets.WSAdapter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class JettyServer {

	private static Server server;
	ServletContextHandler context;
	//private static ServletHandler handler;

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
		ServletContextHandler ctxHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		ctxHandler.setContextPath("/");
		ctxHandler.setResourceBase(System.getProperty("java.io.tmpdir"));
		ctxHandler.addServlet(PageIndex.class, "/");
		ctxHandler.addServlet(PageStatic.class, "/static/*");
		ctxHandler.addServlet(PageFavIcon.class, "/favicon.ico");
		ctxHandler.addServlet(PageDownload.class, "/download");

		ctxHandler.addServlet(RestSearch.class, "/rest/search");
		ctxHandler.addServlet(RestSetupSource.class, "/rest/setup/source/*");
		ctxHandler.addServlet(RestConfig.class, "/rest/setup/config/*");
		ctxHandler.addServlet(RestSetupUser.class, "/rest/setup/user/*");
		ctxHandler.addServlet(RestLink.class, "/rest/link");
		ctxHandler.addServlet(RestVersion.class, "/rest/version");
		ctxHandler.addServlet(RestStats.class, "/rest/stats");
		ctxHandler.addServlet(RestLogin.class, "/rest/login");
		ctxHandler.addServlet(RestLogout.class, "/rest/logout");
		ctxHandler.addServlet(RestQuit.class, "/rest/quit");
		ctxHandler.addServlet(PageOAuth.class, "/oauth/*");

		// Add a websocket to a specific path spec
		ServletHolder holderEvents = new ServletHolder("ws-events", WSServlet.class);
		ctxHandler.addServlet(holderEvents, "/ws");

		server.setHandler(ctxHandler);
	}

	public static class WSServlet extends WebSocketServlet {

		@Override
		public void configure(WebSocketServletFactory factory) {
			factory.register(WSAdapter.class);
		}

	}
}
