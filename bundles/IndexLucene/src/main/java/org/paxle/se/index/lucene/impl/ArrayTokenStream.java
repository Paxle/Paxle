/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

public class ArrayTokenStream extends TokenStream {
	
	private final Object[] data;
	private int pos = 0;
	private int textPos = 0;
	
	public ArrayTokenStream(final Object[] data) {
		this.data = data;
	}
	
	public int getTokenCount() {
		return data.length;
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
