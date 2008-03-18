
package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class PaxleTokenizer extends StandardTokenizer implements Counting {
	
	private int tokenCount;
	private final Log logger = LogFactory.getLog(PaxleTokenizer.class);
	
	public PaxleTokenizer(Reader reader) {
		super(reader);
	}
	
	public PaxleTokenizer(Reader reader, boolean replaceInvalidAcronym) {
		super(reader, replaceInvalidAcronym);
	}
	
	@Override
	public Token next(Token result) throws IOException {
		final Token token = super.next(result);
		tokenCount++;
		return token;
	}
	
	public int getTokenCount() {
		return tokenCount;
	}
}
