
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

import org.paxle.core.data.IDataConsumer;
import org.paxle.core.io.IOTools;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.IIndexIteratable;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;

public class Activator implements BundleActivator {
	
	public static final String CONF_INDEX_LUCENE_PATH_STR = "paxle.index.path";
	
	private static final String DB_PATH = "lucene-db";
	
	public static BundleContext bc = null;
	public static AFlushableLuceneManager lmanager = null;
	public static LuceneWriter indexWriterThread = null;
	public static LuceneSearcher indexSearcher = null;
	public static LuceneQueryFactory tokenFactory = null;
	
	public void start(BundleContext context) throws Exception {
		bc = context;
		final Log logger = LogFactory.getLog(Activator.class);
		
		// check whether directory is locked from previous runs
		final File writeLock = new File(DB_PATH, "write.lock");
		if (writeLock.exists()) {
			logger.warn("Lucene index directory is locked, removing lock. " +
					"Shutdown now if any other lucene-compatible application currently accesses the directory '" +
					writeLock.getPath());
			Thread.sleep(5000l);
			writeLock.delete();
		}
		writeLock.deleteOnExit();
		
		final File stopwordsRoot = context.getDataFile("/stopwords/").getCanonicalFile();
		copyNatives(context, "/stopwords/snowball/", stopwordsRoot);
		final StopwordsManager stopwordsManager = new StopwordsManager(stopwordsRoot);
		
		lmanager = new TimerLuceneManager(DB_PATH, stopwordsManager.getDefaultAnalyzer(), 30000, 30000);
		indexWriterThread = new LuceneWriter(lmanager, stopwordsManager);
		indexWriterThread.setPriority(3);
		indexSearcher = new LuceneSearcher(lmanager);
		
		context.registerService(IIndexWriter.class.getName(), indexWriterThread, new Hashtable<String,String>());
		context.registerService(IIndexSearcher.class.getName(), indexSearcher, new Hashtable<String,String>());
		context.registerService(IIndexIteratable.class.getName(), lmanager, new Hashtable<String,String>());
		
		// publish data source
		final Hashtable<String,String> sinkp = new Hashtable<String,String>();
		sinkp.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
		context.registerService(IDataConsumer.class.getName(), indexWriterThread, sinkp);
		
		Converter.fieldManager = (IFieldManager)context.getService(context.getServiceReference(IFieldManager.class.getName()));
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
		indexWriterThread.close();
		indexSearcher.close();
		Converter.fieldManager = null;
		indexWriterThread = null;
		indexSearcher = null;
		bc = null;
	}
}
