package org.paxle.data.db.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
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
		CommandReader fileReader = new CommandReader(this.getClass().getResourceAsStream("/resources/data.xml"));
		Hashtable<String,String> readerProps = new Hashtable<String, String>();
		readerProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");
		context.registerService(IDataProvider.class.getName(), fileReader, readerProps);
		
		/*
		 * TODO: just for debugging
		 */
		//mwComponent.getDataSink().putData(new Command("http://www.test.at"));
	}

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