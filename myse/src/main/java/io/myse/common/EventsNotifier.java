package io.myse.common;

import io.myse.desktop.TrayIconMgmt;
import static io.myse.common.LOG.LOG;
import io.myse.access.File;
import java.awt.TrayIcon;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EventsNotifier {

	public static class Event {

		public Event(String type) {
			this.type = type;
		}
		final String type;
		final long date = System.currentTimeMillis();
	}

	public static class EventFile extends Event {

		public EventFile(String type, File file) {
			super(type);
			this.path = file.getPath();
			this.source = file.getSource().getDesc().getShortName();
		}

		String path;
		String source;
	}

	public static class EventTextNotification extends Event {

		public EventTextNotification(String title, String message) {
			super("notification");
			this.title = title;
			this.message = message;
		}

		String title, message;
	}

	public static interface EventReceiver {

		void handleEvent(Event event);
	}

	private static final List<Event> events = Collections.synchronizedList(new LinkedList<Event>());
	private static final List<EventReceiver> receivers = Collections.synchronizedList(new ArrayList<EventReceiver>());

	public static void eventScanningNewDir(File dir) {
		EventFile event = new EventFile("newDir", dir);
		addEvent(event);
	}

	public static void eventIndexingFile(File file) {
		EventFile event = new EventFile("newFile", file);
		addEvent(event);
	}

	public static void eventTextNotification(String title, String content) {
		EventTextNotification notification = new EventTextNotification(title, content);
		addEvent(notification);
		TrayIconMgmt.displayMessage(title, content, TrayIcon.MessageType.INFO);
	}

	private static void addEvent(Event event) {
		try {
			events.add(event);
			while (events.size() > 20) {
				events.remove(0);
			}

			for (EventReceiver r : receivers) {
				r.handleEvent(event);
			}
		} catch (Throwable ex) {
			LOG.error("addEvent", ex);
		}
	}

	public static void addReceiver(EventReceiver r) {
		receivers.add(r);
		for (Event e : events) {
			r.handleEvent(e);
		}
	}

	public static void removeReceiver(EventReceiver r) {
		receivers.remove(r);
	}
}
