package org.paxle.core.charset;

import java.io.FilterOutputStream;
import java.io.OutputStream;

public abstract class ACharsetDetectorOutputStream extends FilterOutputStream implements ICharsetDetectorStream {

	public ACharsetDetectorOutputStream(OutputStream out) {
		super(out);
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
