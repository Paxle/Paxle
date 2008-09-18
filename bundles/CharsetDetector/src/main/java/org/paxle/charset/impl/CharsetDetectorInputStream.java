package org.paxle.charset.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.paxle.core.charset.ACharsetDetectorInputStream;

public class CharsetDetectorInputStream extends ACharsetDetectorInputStream implements nsICharsetDetectionObserver {
	
	private final byte[] buffer = new byte[1];
	private String charset = null;
	private nsDetector det = null;
	private boolean done = false;	
	
	public CharsetDetectorInputStream(InputStream in) {
		super(in);
		this.det = new nsDetector(nsPSMDetector.ALL);
		this.det.Init(this);		
	}
	
	/**
	 * @see FilterInputStream
	 */
	@Override
	public int read() throws IOException {
		int b = super.read();
		if (b != -1 && !done) {
			buffer[0] = (byte)b;
			this.det.DoIt(buffer, 1, false);
		}
		return b;
	}
	
	/**
	 * @see FilterInputStream
	 */	
	@Override
	public int read(byte b[]) throws IOException {
		return this.read(b, 0, b.length);
	}

	/**
	 * @see FilterInputStream
	 */	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = super.read(b, off, len);
		if (read != -1 && !done) {
			if (off != 0) {
				byte[] write = new byte[read];
				System.arraycopy(b, off, write, 0, read);
				this.det.DoIt(write, read, false);
			} else {
				this.det.DoIt(b, read, false);
			}
		}
		return read;
	}

	/**
	 * @see FilterInputStream
	 */	
	@Override
	public void close() throws IOException {	
		super.close();
		det.DataEnd();
	}

	/**
	 * @see nsICharsetDetectionObserver
	 */
	public void Notify(String charset) {
		this.charset = charset;
		this.done = true;
	}
	
	/**
	 * @see nsICharsetDetectionObserver
	 */	
	@Override
	public String getCharset() {
		return charset;
	}
	
	/**
	 * @see nsICharsetDetectionObserver
	 */	
	@Override
	public boolean charsetDetected() {
		return this.done;
	}	
}
