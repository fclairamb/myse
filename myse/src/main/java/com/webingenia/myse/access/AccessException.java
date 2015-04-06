package com.webingenia.myse.access;

/**
 * Access exception. This is a general-purpose access exception. It allows to
 * differentiate some general issues:
 * <ul>
 * <li>Access denied (bad credential)</li>
 * <li>Communication error (connection refused or lost)</li>
 * <li>Temporary issue that should be handled with a retry</li>
 * <li>Unknown issue</li>
 * </ul>
 */
public class AccessException extends Exception {

	private final AccessState state;

	public AccessException(AccessState state, Exception ex) {
		super("Access exception", ex);
		this.state = state;
	}

	public enum AccessState {

		DENIED,
		NOT_FOUND,
		ERROR,
		TEMPORARY_ISSUE,
		UNKNOWN
	}
}
