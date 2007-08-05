package org.paxle.se;

public class DBUnitializedException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public DBUnitializedException() {  }
	
	public DBUnitializedException(String message) {
		super(message);
	}
	
	public DBUnitializedException(Throwable cause) {
		super(cause);
	}
	
	public DBUnitializedException(String message, Throwable cause) {
		super(message, cause);
	}
}
