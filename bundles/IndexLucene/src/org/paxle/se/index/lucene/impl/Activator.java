package org.paxle.se.index.lucene.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.se.IIndexSearcher;
import org.paxle.se.IIndexWriter;
import org.paxle.se.query.ITokenFactory;

public class Activator implements BundleActivator {
	
	private static final String DB_PATH = "lucene-db";
	
	public static BundleContext bc = null;
	public static LuceneWriter indexWriter = null;
	public static LuceneSearcher indexSearcher = null;
	public static LuceneTokenFactory tokenFactory = null;
	
	public void start(BundleContext context) throws Exception {
		bc = context;
		indexWriter = LuceneWriter.createWriter(DB_PATH);
		indexSearcher = new LuceneSearcher(DB_PATH);
		tokenFactory = new LuceneTokenFactory();
		
		context.registerService(IIndexWriter.class.getName(), indexWriter, new Hashtable<String,String>());
		context.registerService(IIndexSearcher.class.getName(), indexSearcher, new Hashtable<String,String>());
		context.registerService(ITokenFactory.class.getName(), tokenFactory, new Hashtable<String,String>());
	}
	
	public void stop(BundleContext context) throws Exception {
		indexWriter.close();
		indexSearcher.close();
		indexWriter = null;
		indexSearcher = null;
		tokenFactory = null;
		bc = null;
	}
}
