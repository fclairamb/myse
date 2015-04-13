package com.webingenia.myse.access;

// TODO: Allow to add some generic real-time monitoring of file modifications
public abstract class Monitor {

	public static interface FileEventListener {

	}

	public abstract void start();

	public abstract void stop();

	protected FileEventListener listener;

	public void setListener(FileEventListener listener) {
		this.listener = listener;
	}
}
