package org.paxle.data.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.data.txt.impl.TextCommandReader;

public class Activator implements BundleActivator {
	
	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */	
	public static BundleContext bc = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */
		// this pipe connects the Crawler-Outqueue with the Parser-InQueue
		pipeConnect("org.paxle.crawler.source", "org.paxle.parser.sink");
		
		// another pipe to connect the Parser-OutQueue with the Indexer-InQueue
		pipeConnect("org.paxle.parser.source", "org.paxle.indexer.sink");
		
		// fill the crawler queue with URLs
		TextCommandReader fileReader = new TextCommandReader(this.getClass().getResourceAsStream("/resources/data.txt"));
		Hashtable<String,String> readerProps = new Hashtable<String, String>();
		readerProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");
		context.registerService(IDataProvider.class.getName(), fileReader, readerProps);
		
		/*
		 * Registering the CommandDB
		 * TODO: not finished yet
		 */
//		URL config = context.getBundle().getResource("/resources/hibernate/derby.cfg.xml");
//		CommandDB db = new CommandDB(config);
//		
//		final Hashtable<String,String> props = new Hashtable<String,String>();
//		props.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.parser.source");
//		props.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");		
//		bc.registerService(new String[]{IDataConsumer.class.getName(),IDataProvider.class.getName()}, db, props);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
	
	private static void pipeConnect(String from, String to) {
		final DataPipe pipe = new DataPipe();
		final Hashtable<String,String> props = new Hashtable<String,String>();
		props.put(IDataConsumer.PROP_DATACONSUMER_ID, from);
		props.put(IDataProvider.PROP_DATAPROVIDER_ID, to);
		bc.registerService(new String[]{IDataConsumer.class.getName(),IDataProvider.class.getName()}, pipe, props);
	}
}