package com.webingenia.myse.common;

import java.util.concurrent.ScheduledFuture;

public abstract class RunnableCancellable implements Runnable {

	private ScheduledFuture<?> future;

	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

	@Override
	public abstract void run();

	protected void cancel() {
		future.cancel(false);
	}

}
