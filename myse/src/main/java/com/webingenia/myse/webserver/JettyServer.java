package com.webingenia.myse.webserver;

import org.eclipse.jetty.server.Server;

public class JettyServer {

	private static Server server;

	public static void start() throws Exception {
		server = new Server(8080);
		server.start();
	}

	public static void stop() throws Exception {
		server.stop();
	}
}
