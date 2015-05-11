package io.myse.desktop;

import io.myse.Main;
import io.myse.common.BuildInfo;
import static io.myse.common.LOG.LOG;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class TrayIconMgmt {

	private static TrayIcon icon;

	public static void start() {
		try {
			if (!SystemTray.isSupported()) {
				return;
			}

			PopupMenu trayPopupMenu;
			{
				trayPopupMenu = new PopupMenu();
				MenuItem action = new MenuItem("Quit");
				action.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						Main.quit();
					}
				});
				trayPopupMenu.add(action);
			}

			SystemTray systemTray = SystemTray.getSystemTray();
			try (InputStream is = TrayIconMgmt.class.getResourceAsStream("/icons/icon_032.png")) {
				icon = new java.awt.TrayIcon(ImageIO.read(is), "MySE v" + BuildInfo.VERSION, trayPopupMenu);
			}

			icon.setImageAutoSize(true);
			icon.addMouseListener(new MouseListener() {

				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						Browser.showMyse();
					}
				}

				@Override
				public void mousePressed(MouseEvent e) {
				}

				@Override
				public void mouseReleased(MouseEvent e) {
				}

				@Override
				public void mouseEntered(MouseEvent e) {
				}

				@Override
				public void mouseExited(MouseEvent e) {
				}
			});
			systemTray.add(icon);
		} catch (Throwable ex) {
			LOG.error("TrayIconMgmt.start", ex);
		}
	}

	public static void displayMessage(final String title, final String message, final TrayIcon.MessageType type) {
		if (icon != null) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						icon.displayMessage(title, message, type);
					} catch (Exception ex) {
						LOG.error("displayMessage", ex);
					}
				}
			});
		}
	}
}
