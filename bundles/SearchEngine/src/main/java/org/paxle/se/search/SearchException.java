
package org.paxle.se.search;

public class SearchException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	private final String query;
	
	public SearchException(String query, String message) {
		super(message);
		this.query = query;
	}
	
	public SearchException(String query, Throwable cause) {
		super(cause);
		this.query = query;
	}
	
	public SearchException(String query, String message, Throwable cause) {
		super(message, cause);
		this.query = query;
	}
	
	public String getQuery() {
		return query;
	}
}
