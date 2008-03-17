
package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class PaxleAnalyzer extends StandardAnalyzer {
	
	public PaxleAnalyzer() {
		super();
	}
	
	public PaxleAnalyzer(Set<?> stopWords) {
		super(stopWords);
	}
	
	public PaxleAnalyzer(String[] stopWords) {
		super(stopWords);
	}
	
	public PaxleAnalyzer(File stopwords) throws IOException {
		super(stopwords);
	}
	
	public PaxleAnalyzer(Reader stopwords) throws IOException {
		super(stopwords);
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		super.tokenStream(fieldName, reader);
	    StandardTokenizer tokenStream = new PaxleTokenizer(reader, super.isReplaceInvalidAcronym());
	    tokenStream.setMaxTokenLength(super.getMaxTokenLength());
	    TokenStream result = new StandardFilter(tokenStream);
	    result = new LowerCaseFilter(result);
	    result = new StopFilter(result, STOP_WORDS);
	    return result;
	}
}
