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

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;

/**
 * Filters a tokenizer, e.g. for stopwords
 * 
 * @see StandardAnalyzer
 */
public class PaxleAnalyzer extends StandardAnalyzer {
	
	private ThreadLocal<Map<String, TokenCounter>> counters = new ThreadLocal<Map<String,TokenCounter>>() {
		@Override
		protected Map<String, TokenCounter> initialValue() {
			return new HashMap<String, TokenCounter>();
		}
	};
	
	public PaxleAnalyzer() {
		super(Version.LUCENE_29);
	}
	
	public PaxleAnalyzer(Set<String> stopWords) {
		super(Version.LUCENE_29,stopWords);
	}	
	
	public PaxleAnalyzer(String[] stopWords) {
		super(Version.LUCENE_29,StopFilter.makeStopSet(stopWords));
	}
	
	public void addTokenCounter(String fieldName, TokenCounter counter) {
		Map<String, TokenCounter> counterMap = this.counters.get();
		if (counterMap != null) {
			counterMap.put(fieldName, counter);
		}
	}
	
	public TokenCounter getTokenCounter(String fieldName) {
		final Map<String, TokenCounter> counterMap = this.counters.get();
		if (counterMap == null) return null;		
		return counterMap.get(fieldName);
	}
	
	public Map<String, TokenCounter> getTokenCounters() {
		return counters.get();
	}
	
	public void resetTokenCounters() {
		this.counters.remove();
	}

	public TokenStream wrapDefaultFilters(String fieldName, TokenStream tokenStream) {
		// create a token Counter
		final TokenCounter counter = new TokenCounter();
		this.addTokenCounter(fieldName, counter);
		
		// creating a token-counting filter
		final TokenCountingFilter tokenCountingStream = new TokenCountingFilter(tokenStream);
		tokenCountingStream.setTokenCounter(counter);
		
	    return tokenCountingStream; 
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		// creating the default token stream
		TokenStream tokenStream = super.tokenStream(fieldName, reader);
		
		// wrapping the default token stream with a new filter
		tokenStream = wrapDefaultFilters(fieldName, tokenStream);
		return tokenStream;
	}
}
