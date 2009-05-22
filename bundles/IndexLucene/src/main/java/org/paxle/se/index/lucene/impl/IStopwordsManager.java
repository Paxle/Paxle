package org.paxle.se.index.lucene.impl;
/**
 * This is an bundle-internal interface. Don't export it!
 */
public interface IStopwordsManager {
	/**
	 * Returns a PaxleAnalyzer for the given language
	 * @param language
	 */
	public PaxleAnalyzer getAnalyzer(final String language);
	
	/**
	 * Returns a very simple analyzer for English stopwords.
	 * The stopwords are builtin!
	 */
	public PaxleAnalyzer getDefaultAnalyzer();
}
