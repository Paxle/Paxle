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
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import junit.framework.TestCase;

public class StopwordsManagerTest extends TestCase {
	private StopwordsManager stopwordsmanager;
	
	public static List<URL> getStopwordsFiles() throws MalformedURLException {
		final File stopwordsDir = new File("src/main/resources/stopwords/snowball/");
		assertTrue(stopwordsDir.exists());
		
		final FileFilter stopwordsFileFilter = new WildcardFileFilter("*" + StopwordsManager.STOPWORDS_FILE_EXT);		
		File[] stopwordsFiles = stopwordsDir.listFiles(stopwordsFileFilter);
		assertNotNull(stopwordsFiles);
		
		List<URL> stopwordsURLs = new ArrayList<URL>();
		for (File stopwordsFile : stopwordsFiles) {
			stopwordsURLs.add(stopwordsFile.toURL());
		}
		
		return stopwordsURLs;
	}	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.stopwordsmanager = new StopwordsManager(getStopwordsFiles());
	}
	
	public void testGetAnalyzerDE() {
		PaxleAnalyzer defaultAnalyzer = this.stopwordsmanager.getDefaultAnalyzer();
		assertNotNull(defaultAnalyzer);
		
		PaxleAnalyzer analyzerDE1 = this.stopwordsmanager.getAnalyzer("de");
		assertNotNull(analyzerDE1);
		assertNotSame(defaultAnalyzer, analyzerDE1);
		
		PaxleAnalyzer analyzerDE2 = this.stopwordsmanager.getAnalyzer("DE");
		assertNotNull(analyzerDE2);
		assertSame(analyzerDE1, analyzerDE2);
	}
}
