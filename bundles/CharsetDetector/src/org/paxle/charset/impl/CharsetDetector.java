package org.paxle.charset.impl;

import java.io.InputStream;
import java.io.OutputStream;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.paxle.charset.ICharsetDetector;



public class CharsetDetector implements ICharsetDetector {

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector
	 */
	public CharsetDetectorOutputStream createOutputStream(OutputStream out) {
		return new CharsetDetectorOutputStream(out);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector
	 */	
	public CharsetDetectorInputStream createInputStream(InputStream in) {
		return new CharsetDetectorInputStream(in);
	}

	/**
	 * {@inheritDoc}
	 * @see ICharsetDetector
	 */		
	public String[] getSupportedCharsets() {
		return new nsDetector(nsPSMDetector.ALL).getProbableCharsets();
	}

}
