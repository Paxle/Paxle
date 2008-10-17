
package org.paxle.se.index.lucene.impl;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class ArrayTokenStream extends TokenStream implements Counting {
	
	private final Object[] data;
	private int pos = 0;
	private int textPos = 0;
	
	public ArrayTokenStream(final Object[] data) {
		this.data = data;
	}
	
	public int getTokenCount() {
		return data.length;
	}
	
	public void resetCounts() {
	}
	
	public int getRejectedCount() {
		return 0;
	}
	
	@Override
	public Token next() {
		Object o = null;
		while (o == null && this.pos < this.data.length)
			o = this.data[this.pos++];
		
		if (o == null)
			return null;
		
		int otp = this.textPos;
		final String text = o.toString();
		this.textPos += text.length();
		final Token t = new Token(otp, this.textPos++);
		t.setTermBuffer(text);
		return t;
	}
}
