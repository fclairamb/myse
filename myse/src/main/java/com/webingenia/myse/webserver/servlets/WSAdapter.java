package com.webingenia.myse.webserver.servlets;

import com.google.gson.Gson;
import com.webingenia.myse.common.EventsNotifier;
import static com.webingenia.myse.common.LOG.LOG;
import com.webingenia.myse.tasks.Tasks;
import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

public class WSAdapter extends WebSocketAdapter implements EventsNotifier.EventReceiver {

	public WSAdapter() {
		LOG.info("Created new instance: " + hashCode());
	}

	ScheduledFuture<?> schedule;

	@Override
	public void onWebSocketConnect(Session sess) {
		super.onWebSocketConnect(sess); //To change body of generated methods, choose Tools | Templates.
		LOG.info("Session connected !");
		EventsNotifier.addReceiver(this);
//		schedule = Tasks.getService().scheduleAtFixedRate(new Runnable() {
//
//			int nb;
//
//			@Override
//			public void run() {
//				if (nb >= 10) {
//					schedule.cancel(false);
//				}
//				try {
//					getRemote().sendString("{\"type\":\"hello\"}");
//				} catch (IOException ex) {
//					LOG.error("Comm error", ex);
//				}
//			}
//		}, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	public void onWebSocketText(String message) {
		super.onWebSocketText(message); //To change body of generated methods, choose Tools | Templates.
		LOG.info("Received: " + message);

	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		super.onWebSocketClose(statusCode, reason); //To change body of generated methods, choose Tools | Templates.
		LOG.info("Session disconnected !");
		EventsNotifier.removeReceiver(this);
	}

	private final Gson gson = new Gson();

	@Override
	public void handleEvent(EventsNotifier.Event event) {
		try {
			getRemote().sendString(gson.toJson(event));
		} catch (Exception ex) {
			LOG.error("handleEvent error", ex);
		}
	}
}
