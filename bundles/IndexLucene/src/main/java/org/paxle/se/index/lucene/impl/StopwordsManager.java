/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.StopAnalyzer;

public class StopwordsManager {	
	public static final String STOPWORDS_FILE_EXT = ".stopwords";
	
	private final HashMap<String, URL> urlMap = new HashMap<String, URL>();
	private final HashMap<String,PaxleAnalyzer> analyzerMap = new HashMap<String,PaxleAnalyzer>();
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(StopwordsManager.class);
	
	/**
	 * The default analyzer
	 */
	PaxleAnalyzer defaultAnalyzer = null;
	
	/**
	 * @param stopWordsURLs a list of {@link URL URLs} pointing to the stopwords files to use
	 */
	public StopwordsManager(Collection<URL> stopWordsURLs) {
		if (stopWordsURLs != null) {
			for (URL stopWordsFile : stopWordsURLs) {
				String fileName = stopWordsFile.getFile();
				fileName = fileName.substring(fileName.lastIndexOf('/')+1);
				String lang = fileName.substring(0,fileName.length()-STOPWORDS_FILE_EXT.length());
				
				this.urlMap.put(lang, stopWordsFile);
			}
		}
	}
	
	/**
	 * Returns a PaxleAnalyzer for the given language
	 * @param language
	 */
	public PaxleAnalyzer getAnalyzer(final String language) {
		this.logger.debug("providing analyzer for language '" + language + "'");
		if (language == null) return getDefaultAnalyzer();
		
		PaxleAnalyzer pa = analyzerMap.get(language.toLowerCase());
		if (pa == null) {
			final URL swURL = urlMap.get(language.toLowerCase());			
			if (swURL == null) {
				logger.warn("no stopwords declaration file found for language '" + language + "', falling back to lucene's default");
				return getDefaultAnalyzer();
			}
			
			Reader reader = null;
			try {
				reader = new InputStreamReader(swURL.openStream(),Charset.forName("UTF-8"));
				
				// TODO adapt to multiple formats, i.e. pass wordlist-loader to this instance on creation
				final Set<String> stopwords = MultiFormatWordlistLoader.getSnowballSet(reader);
				pa = new PaxleAnalyzer(stopwords);
				
				analyzerMap.put(language, pa);
			} catch (Throwable e) {
				logger.error(String.format(
						"Unexpected %s reading in stopwords declaration '%s', falling back to lucene's default.",
						e.getClass().getName(),
						swURL,
						e.getMessage()
				),e);
			} finally {
				if (reader != null) try { reader.close(); } catch (Exception e) {/* ignore this */}
			}
		}
		return (pa!=null)?pa:getDefaultAnalyzer();
	}
	
	/**
	 * Returns a very simple analyzer for English stopwords.
	 * The stopwords are builtin!
	 */
	public PaxleAnalyzer getDefaultAnalyzer() {
		if (defaultAnalyzer == null)
			defaultAnalyzer = new PaxleAnalyzer(StopAnalyzer.ENGLISH_STOP_WORDS);
		return defaultAnalyzer;
	}
}
