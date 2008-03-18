
package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IIndexerDocument;

public class StopwordsManager {
	
	public static final String STOPWORDS_FILE_EXT = ".stopwords";
	
	private final HashMap<IIndexerDocument.Language,PaxleAnalyzer> map = new HashMap<IIndexerDocument.Language,PaxleAnalyzer>();
	private final File root;
	private final Log logger = LogFactory.getLog(StopwordsManager.class);
	private PaxleAnalyzer defaultAnalyzer = null;
	
	public StopwordsManager(final File root) {
		this.root = root;
	}
	
	public PaxleAnalyzer getAnalyzer(final IIndexerDocument.Language language) {
		if (language == null)
			return getDefaultAnalyzer();
		PaxleAnalyzer pa = map.get(language);
		if (pa == null) {
			final File swFile = new File(root, language.name() + STOPWORDS_FILE_EXT);
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
	
	public PaxleAnalyzer getDefaultAnalyzer() {
		if (defaultAnalyzer == null)
			defaultAnalyzer = new PaxleAnalyzer();
		return defaultAnalyzer;
	}
}
