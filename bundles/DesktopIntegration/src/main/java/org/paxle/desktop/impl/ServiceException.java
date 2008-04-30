
package org.paxle.desktop.impl;

public class ServiceException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public ServiceException(final String name, final String symbolicName) {
		super("'" + symbolicName + "' ('" + name + "') not available");
	}
	
	public ServiceException(final String name, final String symbolicName, final Throwable cause) {
		super("'" + symbolicName + "' ('" + name + "') not available", cause);
	}
}
