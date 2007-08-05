package org.paxle.se.index.lucene.impl;

import java.io.File;
import java.util.Hashtable;

import org.apache.lucene.index.IndexWriter;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.core.data.IDataConsumer;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.query.ITokenFactory;

public class Activator implements BundleActivator {
	
	private static final String DB_PATH = "lucene-db";
	
	public static BundleContext bc = null;
	public static IndexWriter indexWriter = null;
	public static LuceneWriter indexWriterThread = null;
	public static LuceneSearcher indexSearcher = null;
	public static LuceneTokenFactory tokenFactory = null;
	
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		// check whether directory is locked from previous runs
		
		final File writeLock = new File(DB_PATH, "write.lock");
		writeLock.deleteOnExit();
		
		indexWriter = LuceneWriter.createWriter(DB_PATH);
		indexWriterThread = new LuceneWriter(indexWriter);
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
		indexWriterThread.interrupt();
		indexWriterThread.join();
		indexWriterThread = null;
		indexWriter.close();
		indexWriter = null;
		indexSearcher.close();
		indexSearcher = null;
		tokenFactory = null;
		bc = null;
	}
}
