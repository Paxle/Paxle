
package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class PaxleTokenizer extends StandardTokenizer implements Counting {
	
	private int tokenCount;
	private boolean isCounted = false;
	
	public PaxleTokenizer(Reader reader) {
		super(reader);
	}
	
	public PaxleTokenizer(Reader reader, boolean replaceInvalidAcronym) {
		super(reader, replaceInvalidAcronym);
	}
	
	@Override
	public Token next() throws IOException {
		final Token token = super.next();
		if (isCounted) {
			isCounted = false;
		} else {
			isCounted = true;
			tokenCount++;
		}
		return token;
	}
	
	@Override
	public Token next(Token result) throws IOException {
		final Token token = super.next(result);
		if (isCounted) {
			isCounted = false;
		} else {
			isCounted = true;
			tokenCount++;
		}
		return token;
	}
	
	public int getTokenCount() {
		return tokenCount;
	}
}
