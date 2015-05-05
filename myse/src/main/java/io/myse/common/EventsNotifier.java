package io.myse.common;

import io.myse.access.AccessException;
import static io.myse.common.LOG.LOG;
import io.myse.access.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class EventsNotifier {

	public static class Event {

		final String message;
		final long date = System.currentTimeMillis();

		public Event(String message) {
			this.message = message;
		}
	}

	public static interface EventReceiver {

		void handleEvent(Event event);
	}

	private static final List<Event> events = Collections.synchronizedList(new LinkedList<Event>());
	private static final List<EventReceiver> receivers = Collections.synchronizedList(new ArrayList<EventReceiver>());

	public static void eventScanningNewDir(File dir) {
		addEvent(String.format("A new directory was discovered: ", dir));
	}

	public static void eventIndexingFile(File file) {
		try {
			addEvent(String.format("A new file was discovered: %s (%s)", file.getName(), file.getPath()));
		} catch (AccessException ex) {
			LOG.error("EventsNotifier", ex);
		}
	}

	public static void addEvent(String message) {
		addEvent(new Event(message));
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
