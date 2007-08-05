package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.core.data.IDataConsumer;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.query.ITokenFactory;

public class Activator implements BundleActivator {
	
	private static final String DB_PATH = "lucene-db";	// TODO
	
	public static BundleContext bc = null;
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
		
		indexWriterThread = new LuceneWriter(DB_PATH);
		indexSearcher = new LuceneSearcher(DB_PATH);
		tokenFactory = new LuceneTokenFactory();
		
		context.registerService(IIndexWriter.class.getName(), indexWriterThread, new Hashtable<String,String>());
		context.registerService(IIndexSearcher.class.getName(), indexSearcher, new Hashtable<String,String>());
		context.registerService(ITokenFactory.class.getName(), tokenFactory, new Hashtable<String,String>());
		
		// publish data source
		final Hashtable<String,String> sinkp = new Hashtable<String,String>();
		sinkp.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
		context.registerService(IDataConsumer.class.getName(), indexWriterThread, sinkp);
	}
	 
	public void stop(BundleContext context) throws Exception {
		indexWriterThread.close();
		indexSearcher.close();
		indexWriterThread = null;
		indexSearcher = null;
		tokenFactory = null;
		bc = null;
	}
}
