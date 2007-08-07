package org.paxle.charset.impl;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.paxle.charset.ACharsetDetectorOutputStream;


public class CharsetDetectorOutputStream extends ACharsetDetectorOutputStream implements nsICharsetDetectionObserver {

	private String charset = null;
	private nsDetector det = null;
	private boolean done = false;

	public CharsetDetectorOutputStream(OutputStream out) {
		super(out);
		this.det = new nsDetector(nsPSMDetector.ALL);
		this.det.Init(this);
	}

	/**
	 * @see FilterOutputStream
	 */
	@Override
	public void write(int b) throws IOException {	
		this.det.DoIt(new byte[]{(byte)b},1, false);
		super.write(b);
	}	

	/**
	 * @see FilterOutputStream
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
