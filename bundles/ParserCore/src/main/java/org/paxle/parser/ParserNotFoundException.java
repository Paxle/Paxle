
package org.paxle.parser;

public class ParserNotFoundException extends ParserException {
	
	private static final long serialVersionUID = 1L;
	
	public ParserNotFoundException(String mimeType) {
		super(String.format("Sub-parser for MIME-type '%s' not available", mimeType));
	}
	
	public ParserNotFoundException(String mimeType, Throwable cause) {
		super(String.format("Sub-parser for MIME-type '%s' not available", mimeType), cause);
	}
}
