package org.paxle.data.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.filter.IFilter;
import org.paxle.data.db.impl.CommandDB;
import org.paxle.data.db.impl.UrlExtractorFilter;
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
		URL config = null;

		if (false) {
			/* Getting the config file to use 
			 * Note: we do not use class.getName() because the PreferencesSerivce  is declared as optional
			 */
			String configStr = System.getProperty("org.paxle.data.db.impl.CommandDB");
			if (configStr != null) {
				config = new URL(configStr);				
			} else {						
				config = context.getBundle().getResource("/resources/hibernate/derby.cfg.xml");
			}

			// getting the mapping files to use
			Enumeration<URL> mappingFileEnum = context.getBundle().findEntries("/resources/hibernate/mapping/", "*.hbm.xml", true);
			ArrayList<URL> mappings = Collections.list(mappingFileEnum);

			// init command DB
			CommandDB db = new CommandDB(config, mappings);		

			final Hashtable<String,String> props = new Hashtable<String,String>();
//			props.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
			props.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");		
			bc.registerService(new String[]{IDataConsumer.class.getName(),IDataProvider.class.getName()}, db, props);

			// register filter
			Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
			filterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{"org.paxle.parser.out; pos=" + Integer.MAX_VALUE});
			bc.registerService(IFilter.class.getName(), new UrlExtractorFilter(db), filterProps);	

			// start the commandDB
			db.start();
		}
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
		pipe.setName(String.format("Datapipe: %s -> %s", from,to));
		final Hashtable<String,String> props = new Hashtable<String,String>();
		props.put(IDataConsumer.PROP_DATACONSUMER_ID, from);
		props.put(IDataProvider.PROP_DATAPROVIDER_ID, to);
		bc.registerService(new String[]{IDataConsumer.class.getName(),IDataProvider.class.getName()}, pipe, props);
	}
}