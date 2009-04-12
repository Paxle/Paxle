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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.monitor.Monitorable;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.io.IOTools;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.IIndexIteratable;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.search.ISearchProvider;

public class Activator implements BundleActivator {
	
	public static final String CONF_INDEX_LUCENE_PATH_STR = "paxle.index.path";
	
	private static final String DB_PATH = "lucene-db";
	
	private AFlushableLuceneManager lmanager = null;
	private LuceneWriter indexWriterThread = null;
	private LuceneSearcher indexSearcher = null;
	private SnippetFetcher snippetFetcher = null;
	
	@SuppressWarnings("serial")
	public void start(BundleContext bc) throws Exception {
		final Log logger = LogFactory.getLog(Activator.class);
		
		// getting the command-tracker
		ServiceReference commandTrackerRef = bc.getServiceReference(ICommandTracker.class.getName());
		ICommandTracker commandTracker = (commandTrackerRef == null) ? null : (ICommandTracker)bc.getService(commandTrackerRef);
		if (commandTracker == null) {
			logger.warn("No CommandTracker-service found. Command-tracking will not work.");
		}
		
		// check whether directory is locked from previous runs
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + DB_PATH;
		final File writeLock = new File(dataPath, "write.lock");
		if (writeLock.exists()) {
			logger.warn("Lucene index directory is locked, removing lock. " +
					"Shutdown now if any other lucene-compatible application currently accesses the directory '" +
					writeLock.getPath());
			Thread.sleep(5000l);
			writeLock.delete();
		}
		writeLock.deleteOnExit();
		
		final File stopwordsRoot = bc.getDataFile("stopwords/").getCanonicalFile();
		copyNatives(bc, "/stopwords/snowball/", stopwordsRoot);
		final StopwordsManager stopwordsManager = new StopwordsManager(stopwordsRoot);
		
		lmanager = new AFlushableLuceneManager(dataPath, stopwordsManager.getDefaultAnalyzer());
		indexWriterThread = new LuceneWriter(lmanager, stopwordsManager, commandTracker);
		indexWriterThread.setPriority(3);
		snippetFetcher = new SnippetFetcher(bc, stopwordsManager.getDefaultAnalyzer());
		indexSearcher = new LuceneSearcher(lmanager, snippetFetcher);
		
		bc.registerService(IIndexWriter.class.getName(), indexWriterThread, new Hashtable<String,String>());
		
		bc.registerService(new String[] {
				IIndexSearcher.class.getName(),
				ISearchProvider.class.getName(),
				Monitorable.class.getName()
		}, indexSearcher, new Hashtable<String,Object>(){{
			// service ID
			put(Constants.SERVICE_PID, LuceneSearcher.PID);
			
			// monitorable properties
			put("Monitorable-Localization", "/OSGI-INF/l10n/LuceneSearcher");
			
			// meta-data properties
			put("org.paxle.metadata",Boolean.TRUE);
			put("org.paxle.metadata.localization","/OSGI-INF/l10n/LuceneSearcher");			
		}});
		
		bc.registerService(IIndexIteratable.class.getName(), lmanager, new Hashtable<String,String>());
		
		// publish data source
		final Hashtable<String,String> sinkp = new Hashtable<String,String>();
		sinkp.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
		bc.registerService(IDataConsumer.class.getName(), indexWriterThread, sinkp);
		
		Converter.fieldManager = (IFieldManager)bc.getService(bc.getServiceReference(IFieldManager.class.getName()));
	}
	
	@SuppressWarnings("unchecked")
	private static void copyNatives(final BundleContext context, final String searchPath, final File root) throws IOException {
		if (!root.exists())
			root.mkdirs();
		
		final Enumeration<URL> stopwords = context.getBundle().findEntries(searchPath, "*" + StopwordsManager.STOPWORDS_FILE_EXT, true);
		while (stopwords.hasMoreElements()) {
			final URL swFileURL = stopwords.nextElement();
			
			// open a file
			String fileName = swFileURL.getFile();
			if (fileName.endsWith("/"))
				continue;
			
			final File swFile = new File(root, fileName.substring(fileName.lastIndexOf('/') + 1));		
			
			if (!swFile.exists() && !fileName.endsWith("/")) {
				final File parent = swFile.getParentFile();
				if (!parent.exists())
					parent.mkdirs();
				
				final FileOutputStream out = new FileOutputStream(swFile);
				// open the URL and copy everything to the new file
				IOTools.copy(swFileURL.openStream(), out);
				out.close();
			}
		}
	}
	
	public void stop(BundleContext context) throws Exception {
		lmanager.close();
		snippetFetcher.close();
		indexWriterThread.close();
		indexSearcher.close();
		Converter.fieldManager = null;
		indexWriterThread = null;
		indexSearcher = null;
	}
}
