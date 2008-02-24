
package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.core.data.IDataConsumer;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.IIndexIteratable;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;

public class Activator implements BundleActivator {
	
	public static final String CONF_INDEX_LUCENE_PATH_STR = "paxle.index.path";
	
	private static String DB_PATH = "lucene-db";
	
	public static BundleContext bc = null;
	public static AFlushableLuceneManager lmanager = null;
	public static LuceneWriter indexWriterThread = null;
	public static LuceneSearcher indexSearcher = null;
	public static LuceneTokenFactory tokenFactory = null;
	
	public void start(BundleContext context) throws Exception {
		bc = context;
		final Log logger = LogFactory.getLog(Activator.class);
		
		// check whether directory is locked from previous runs
		final File writeLock = new File(DB_PATH, "write.lock");
		if (writeLock.exists()) {
			logger.warn("Lucene index directory is locked, removing lock. " +
					"Shutdown now if any other lucene-compatible application currently accesses the directory '" +
					writeLock.getPath());
			writeLock.delete();
		}
		writeLock.deleteOnExit();
		
		lmanager = new TimerLuceneManager(DB_PATH, 30000, 30000);
		indexWriterThread = new LuceneWriter(lmanager);
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
