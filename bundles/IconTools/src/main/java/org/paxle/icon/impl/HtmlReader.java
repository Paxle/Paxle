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
		this.closed = true;
		super.close();
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
