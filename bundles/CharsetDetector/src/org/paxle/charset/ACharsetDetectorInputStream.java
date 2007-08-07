package org.paxle.charset;

import java.io.FilterInputStream;
import java.io.InputStream;

public abstract class ACharsetDetectorInputStream extends FilterInputStream implements ICharsetDetectorStream {
	protected ACharsetDetectorInputStream(InputStream in) {
		super(in);
	}
	
	/**
	 * {@inheritDoc}
	 * @see ICharsetDetectorStream
	 */
	public abstract String getCharset();
	
	/**
	 * {@inheritDoc}
	 * @see ICharsetDetectorStream
	 */	
	public abstract boolean charsetDetected();
}
