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

package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class PaxleTokenizer extends StandardTokenizer implements Counting {
	
	private int tokenCount;
	
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
	
	public void resetCounts() {
		tokenCount = 0;
	}
}
