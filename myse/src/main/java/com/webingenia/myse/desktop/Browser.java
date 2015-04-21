package com.webingenia.myse.desktop;

import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.db.model.Config;
import com.webingenia.myse.webserver.JettyServer;
import java.awt.Desktop;
import java.net.URI;

public class Browser {

	public static void showMyse() {
		show("http://localhost:" + Config.get(JettyServer.PROP_PORT, 10080, false) + "/");
	}

	public static void show(String url) {
		try {
			if (Desktop.isDesktopSupported()) {
				Desktop.getDesktop().browse(new URI(url));
			}
		} catch (Exception ex) {
			LOG.error("Browser issue", ex);
		}
	}

}
