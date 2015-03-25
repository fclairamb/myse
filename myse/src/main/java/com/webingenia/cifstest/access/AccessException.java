package com.webingenia.cifstest.access;

public class AccessException extends Exception {

	private final AccessState state;

	public AccessException(AccessState state, Exception ex) {
		super("Access exception", ex);
		this.state = state;
	}

	public enum AccessState {

		OK,
		DENIED,
		ERROR,
		TEMPORARY_ISSUE,
		UNKNOWN
	}
}
