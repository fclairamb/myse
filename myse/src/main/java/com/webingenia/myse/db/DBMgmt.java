package com.webingenia.myse.db;

import static com.webingenia.myse.common.LOG.LOG;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

public class DBMgmt {

	private static boolean stopped;

	static {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException ex) {
			LOG.error("Could not load H2 Driver !", ex);
		}
	}

	private static String getJdbcServerUrl() {
		return "jdbc:h2:tcp://localhost/~/.myse/h2/main";
	}

	private static String getJdbcLocalUrl() {
		return "jdbc:h2:file:~/.myse/h2/main";
	}

	private static String getJdbcUrl() {
		return serverMode ? getJdbcServerUrl() : getJdbcLocalUrl();
	}

	private static boolean serverMode;

	public void setServerMode(boolean mode) {
		serverMode = mode;
	}

	private static boolean httpServer = true;

	public void setHttpServer(boolean mode) {
		httpServer = mode;
	}

	private static Server h2Server;

	public synchronized static void startH2Server() throws SQLException {
		//String path = getH2DB().getAbsolutePath();
		LOG.debug("Starting H2 server...");
		if (h2Server == null) {
			h2Server = Server.createTcpServer(new String[]{"-tcp"});
			h2Server.start();

			Server webServer = Server.createWebServer(new String[]{});
			webServer.start();
		}
	}

	public synchronized static void stopH2Server() {
		LOG.debug("Stopping H2 server...");
		if (h2Server != null) {
			h2Server.stop();
			h2Server = null;
		}
	}

	private static JdbcConnectionPool pool;

	private static JdbcConnectionPool getPool() {
		if (pool == null) {
			if (stopped) {
				throw new RuntimeException("DB is stopped !");
			}
			pool = JdbcConnectionPool.create(getJdbcUrl(), "sa", "");
		}
		return pool;
	}

	public static synchronized Connection getConnection() throws SQLException {
		try {
			return getPool().getConnection();
		} catch (SQLException ex) {
			pool.dispose();
			pool = null;
		}
		return getPool().getConnection();
	}

	public static void start() throws SQLException {
		stopped = false;
		if (serverMode || httpServer) {
			startH2Server();
		}
	}

	public static void stop() {
		stopped = true;
		if (serverMode || httpServer) {
			stopH2Server();
		}
		if (pool != null) {
			pool.dispose();
			pool = null;
		}
	}

	private static EntityManagerFactory emf;

	public static EntityManagerFactory getEntityManagerFactory() {
		if (emf == null) {
			HashMap<String, String> properties = new HashMap<>();
			properties.put("eclipselink.jdbc.driver", "org.h2.Driver");
			properties.put("eclipselink.jdbc.user", "sa");
			properties.put("eclipselink.jdbc.password", "");
			properties.put("eclipselink.jdbc.url", serverMode ? getJdbcServerUrl() : getJdbcLocalUrl());
			emf = Persistence.createEntityManagerFactory("h2");
		}
		return emf;
	}

	public static EntityManager getEntityManager() {
		return getEntityManagerFactory().createEntityManager();
	}
}
