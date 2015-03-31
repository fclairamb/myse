package com.webingenia.myse.webserver;

import com.webingenia.myse.webserver.servlets.PageIndex;
import com.webingenia.myse.webserver.servlets.PageStatic;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

public class JettyServer {

	private static Server server;
	private static ServletHandler handler;

	public static void start() throws Exception {
		server = new Server(8080);

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
		server.setHandler(handler);
	}
}
