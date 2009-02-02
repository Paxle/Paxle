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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.StopAnalyzer;

public class StopwordsManager {
	
	public static final String STOPWORDS_FILE_EXT = ".stopwords";
	
	private final HashMap<String,PaxleAnalyzer> map = new HashMap<String,PaxleAnalyzer>();
	private final Log logger = LogFactory.getLog(StopwordsManager.class);
	private final File rootdir;
	private PaxleAnalyzer defaultAnalyzer = null;
	
	/**
	 * @param rootdir The root-directory where the stopword files are in
	 */
	public StopwordsManager(final File rootdir) {
		this.rootdir = rootdir;
	}
	
	/**
	 * Returns a PaxleAnalyzer for the given language
	 * @param language
	 */
	public PaxleAnalyzer getAnalyzer(final String language) {
		logger.debug("providing analyzer for language '" + language + "'");
		if (language == null)
			return getDefaultAnalyzer();
		PaxleAnalyzer pa = map.get(language);
		if (pa == null) {
			final File swFile = new File(rootdir, language + STOPWORDS_FILE_EXT);
			if (!swFile.exists()) {
				logger.warn("no stopwords declaration file found for language '" + language + "', falling back to lucene's default");
				return getDefaultAnalyzer();
			}
			try {
				// TODO adapt to multiple formats, i.e. pass wordlist-loader to this instance on creation
				final Set<String> stopwords = MultiFormatWordlistLoader.getSnowballSet(swFile);
				pa = new PaxleAnalyzer(stopwords);
				map.put(language, pa);
			} catch (IOException e) {
				logger.error("Error reading in stopwords declaration file '" + swFile + "': " + e.getMessage() + ", falling back to lucene's default");
				return getDefaultAnalyzer();
			}
		}
		return pa;
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
