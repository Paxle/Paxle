
package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
/**
 * Filters a tokenizer, e.g. for stopwords
 * 
 * @see StandardAnalyzer
 */
public class PaxleAnalyzer extends StandardAnalyzer {
	
	private final String[] stopWords;
	
	public PaxleAnalyzer(Set<?> stopWords) {
		this.stopWords = stopWords.toArray(new String[stopWords.size()]);
	}
	
	public PaxleAnalyzer(String[] stopWords) {
		this.stopWords = stopWords;
	}
	
	public PaxleTokenizer createTokenizer(final Reader reader, final boolean replaceInvalidAcronym) {
		return new PaxleTokenizer(reader, replaceInvalidAcronym);
	}
	
	public PaxleTokenizer createTokenizer(final Reader reader) {
		return new PaxleTokenizer(reader);
	}
	
	public TokenStream wrapDefaultFilters(final TokenStream tokenStream, final boolean standardFilter) {
	    TokenStream result = new StandardFilter(tokenStream);
	    if (standardFilter)
	    	result = new StandardFilter(result);
	    result = new LowerCaseFilter(result);
	    result = new StopFilter(result, stopWords);
	    return result; 
	}
	
	/* the code below is partly copied from StandardTokenizer, which unfortunately does not
	 * simply use an extra factory method to obtain a new instance of the StandardTokenizer
	 * it returns */
	
	@Override
	@SuppressWarnings("deprecation")	// needed until isReplaceInvalidAcronym becomes hardwired to "true" in lucene 3.0
	public TokenStream tokenStream(String fieldName, Reader reader) {
	    PaxleTokenizer tokenStream = createTokenizer(reader, super.isReplaceInvalidAcronym());
	    tokenStream.setMaxTokenLength(super.getMaxTokenLength());
	    return wrapDefaultFilters(tokenStream, false);
	}
	
	protected static class SavedStreams {
		PaxleTokenizer tokenStream;
		TokenStream filteredTokenStream;
	}
	
	@Override
	@SuppressWarnings("deprecation")
	public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
		SavedStreams streams = (SavedStreams)getPreviousTokenStream();
		if (streams == null) {
			streams = new SavedStreams();
			setPreviousTokenStream(streams);
			streams.tokenStream = createTokenizer(reader);
			streams.filteredTokenStream = wrapDefaultFilters(streams.tokenStream, true);
		} else {
			streams.tokenStream.reset(reader);
		}
		streams.tokenStream.setMaxTokenLength(super.getMaxTokenLength());
		
		streams.tokenStream.setReplaceInvalidAcronym(super.isReplaceInvalidAcronym());
		
		return streams.filteredTokenStream;
	}
}
