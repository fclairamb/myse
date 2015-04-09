package com.webingenia.myse.common;

import com.webingenia.myse.access.File;
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

	public static interface EventReceiver {

		void handleEvent(Event event);
	}

	private static final List<Event> events = Collections.synchronizedList(new LinkedList<Event>());
	private static final List<EventReceiver> receivers = Collections.synchronizedList(new ArrayList<EventReceiver>());

	public static void scanningNewDir(File dir) {
		EventFile event = new EventFile("newDir", dir);
		addEvent(event);
	}

	public static void indexingFile(File file) {
		EventFile event = new EventFile("newFile", file);
		addEvent(event);
	}

	private static void addEvent(Event event) {
		try {
			events.add(event);
			while (events.size() > 10) {
				events.remove(0);
			}

			for (EventReceiver r : receivers) {
				r.handleEvent(event);
			}
		} catch (Throwable ex) {
			LOG.LOG.error("addEvent", ex);
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
