package org.paxle.se.index.lucene.impl;

import java.util.Hashtable;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.se.IIndexSearcher;
import org.paxle.se.IIndexWriter;

public class Activator implements BundleActivator {
	
	private static final String DB_PATH = "lucene-db";
	
	public static BundleContext bc = null;
	public static LuceneWriter iw = null;
	public static LuceneSearcher is = null;
	
	public void start(BundleContext context) throws Exception {
		bc = context;
		iw = new LuceneWriter(DB_PATH, new StandardAnalyzer());
		is = new LuceneSearcher(DB_PATH);
		
		context.registerService(IIndexWriter.class.getName(), iw, new Hashtable<String,String>());
		context.registerService(IIndexSearcher.class.getName(), is, new Hashtable<String,String>());
	}
	
	public void stop(BundleContext context) throws Exception {
		iw.close();
		is.close();
		iw = null;
		is = null;
		bc = null;
	}
}
