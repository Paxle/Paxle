package org.paxle.filter.blacklist.impl;

/**
 * This exception should be thrown when a name for a blacklist contains dots (directory traversal) or other invalid characters
 */
 @SuppressWarnings("serial")
public class InvalidFilenameException extends Exception 
{

	public InvalidFilenameException() 
	{ 
		super();
	} 

	public InvalidFilenameException( final String s ) 
	{ 
		super(s); 
	} 
}
