package com.webingenia.myse.webserver;

import com.webingenia.myse.common.LOG;
import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.webserver.servlets.PageDownload;
import com.webingenia.myse.webserver.servlets.PageIndex;
import com.webingenia.myse.webserver.servlets.PageStatic;
import com.webingenia.myse.webserver.servlets.RestQuit;
import com.webingenia.myse.webserver.servlets.RestSearch;
import com.webingenia.myse.webserver.servlets.RestSetupSource;
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
		handler.addServletWithMapping(RestSetupSource.class, "/rest/setup/source/*");
		handler.addServletWithMapping(RestVersion.class, "/rest/version");
		handler.addServletWithMapping(RestQuit.class, "/rest/quit");
		handler.addServletWithMapping(PageDownload.class, "/download");

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
