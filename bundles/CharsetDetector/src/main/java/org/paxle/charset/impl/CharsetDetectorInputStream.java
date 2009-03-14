/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.charset.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.paxle.core.charset.ACharsetDetectorInputStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

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
	@SuppressWarnings("NM_METHOD_NAMING_CONVENTION")
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
