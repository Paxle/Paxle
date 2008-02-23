package org.paxle.se.index;

import java.io.IOException;

public class IndexException extends IOException {
	
	private static final long serialVersionUID = 1L;
	
	public IndexException() {  }
	
	public IndexException(String message) {
		super(message);
	}
	
	public IndexException(Throwable cause) {
		super.initCause(cause);
	}
	
	public IndexException(String message, Throwable cause) {
		super(message);
		super.initCause(cause);
	}
}
