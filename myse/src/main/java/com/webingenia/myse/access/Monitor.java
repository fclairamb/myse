package com.webingenia.myse.access;

/**
 * Realtime monitoring of files modifications.
 *
 */
public abstract class Monitor {

	// TODO: Allow to add some generic real-time monitoring of file modifications
	public static interface FileEventListener {

	}

	public abstract void start();

	public abstract void stop();

	protected FileEventListener listener;

	public void setListener(FileEventListener listener) {
		this.listener = listener;
	}
}
