/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.icon.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.text.html.parser.ParserDelegator;

/**
 * Just a wrapper around the input- stream to ensure
 * that a call of {@link #close()} by the {@link HtmlParserCallback}
 * does not result in an {@link IOException} within the 
 * {@link ParserDelegator}.
 */
public class HtmlReader extends InputStreamReader {
	private boolean closed = false;
	
	public HtmlReader(InputStream in) {
		super(in);
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		this.closed = true;
	}

	@Override
	public int read() throws IOException {
		if (this.closed) return -1;
		return super.read();
	}
	
	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (this.closed) return -1;
		return super.read(cbuf, off, len);
	}

}
