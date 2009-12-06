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

import java.io.IOException;
import java.io.OutputStream;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;
import org.paxle.core.charset.ACharsetDetectorOutputStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class CharsetDetectorOutputStream extends ACharsetDetectorOutputStream implements nsICharsetDetectionObserver {
	
	private final byte[] buffer = new byte[1];
	private String charset = null;
	private nsDetector det = null;
	private boolean done = false;

	public CharsetDetectorOutputStream(OutputStream out) {
		super(out);
		this.det = new nsDetector(nsPSMDetector.ALL);
		this.det.Init(this);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		if (!done) {
			final byte[] buf;
			if (off == 0) {
				buf = b;
			} else {
				buf = new byte[len];
				System.arraycopy(b, off, buf, 0, len);
			}
			this.det.DoIt(buf, len, false);
		}
		super.write(b, off, len);
	}
	
	@Override
	public void write(byte[] b) throws IOException {
		if (!done)
			this.det.DoIt(b, b.length, false);
		super.write(b);
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(int b) throws IOException {
		if (!done) {
			buffer[0] = (byte)b;
			this.det.DoIt(buffer, 1, false);
		}
		super.write(b);
	}	

	/*
	 * (non-Javadoc)
	 * @see java.io.FilterOutputStream#close()
	 */
	@Override
	public void close() throws IOException {	
		super.close();
		det.DataEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see org.mozilla.intl.chardet.nsICharsetDetectionObserver#Notify(java.lang.String)
	 */
	@SuppressWarnings("NM_METHOD_NAMING_CONVENTION")
	public void Notify(String charset) {
		this.charset = charset;
		this.done = true;
	}

	/*
	 * 	(non-Javadoc)
	 * @see org.paxle.core.charset.ACharsetDetectorOutputStream#getCharset()
	 */
	@Override
	public String getCharset() {
		return charset;
	}

	/*
	 * 	(non-Javadoc)
	 * @see org.paxle.core.charset.ACharsetDetectorOutputStream#charsetDetected()
	 */
	@Override
	public boolean charsetDetected() {
		return this.done;
	}
}
