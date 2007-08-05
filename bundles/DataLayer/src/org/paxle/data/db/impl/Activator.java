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
		/*
		DataPipe crawlerToparserPipe = new DataPipe();
		Hashtable<String,String> crawlerToparserPipeProps = new Hashtable<String, String>();
		crawlerToparserPipeProps.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.crawler.source");
		crawlerToparserPipeProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.parser.sink");
		
		context.registerService(IDataProvider.class.getName(), crawlerToparserPipe, crawlerToparserPipeProps);
		context.registerService(IDataConsumer.class.getName(), crawlerToparserPipe, crawlerToparserPipeProps);
		*/
		
		// another pipe to connect the Parser-OutQueue with the Indexer-InQueue
		pipeConnect("org.paxle.parser.source", "org.paxle.indexer.sink");
		/*
		DataPipe parserToIndexerPipe = new DataPipe();
		Hashtable<String,String> parserToIndexerPipeProps = new Hashtable<String, String>();
		parserToIndexerPipeProps.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.parser.source");
		parserToIndexerPipeProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.indexer.sink");
		
		context.registerService(IDataProvider.class.getName(), parserToIndexerPipe, parserToIndexerPipeProps);
		context.registerService(IDataConsumer.class.getName(), parserToIndexerPipe, parserToIndexerPipeProps);
		*/
		
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
		bc.registerService(IDataConsumer.class.getName(), pipe, props);
		bc.registerService(IDataProvider.class.getName(), pipe, props);
	}
}