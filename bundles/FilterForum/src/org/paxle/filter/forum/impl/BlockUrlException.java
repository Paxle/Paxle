package org.paxle.filter.forum.impl;

public class BlockUrlException extends Exception {
	private static final long serialVersionUID = 1L;

	public BlockUrlException() {
		super();
	}
	
	public BlockUrlException(String message) {
		super(message);
	}
}
