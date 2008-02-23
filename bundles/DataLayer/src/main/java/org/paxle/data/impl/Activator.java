package org.paxle.data.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	 * Logger
	 */
	private Log logger = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;

		// init logger
		this.logger = LogFactory.getLog(this.getClass());
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */
		this.logger.info("Initializing pipes ...");
		
		// this pipe connects the Crawler-Outqueue with the Parser-InQueue
		pipeConnect("org.paxle.crawler.source", "org.paxle.parser.sink");

		// another pipe to connect the Parser-OutQueue with the Indexer-InQueue
		pipeConnect("org.paxle.parser.source", "org.paxle.indexer.sink");

		/*
		 * Registering the CommandDB
		 * TODO: not finished yet
		 */
		URL config = null;

		if (true) {
			this.classLoaderCheck();
			
			this.logger.info("Trying to find db config files ...");
			Enumeration<URL> configFileEnum = context.getBundle().findEntries("/resources/hibernate/", "*.cfg.xml", true);
			if (configFileEnum != null) {				
				ArrayList<URL> configFiles = Collections.list(configFileEnum);
				this.logger.info(String.format("%d config-files found.",configFiles.size()));
			} else {
				this.logger.info("No config files found");
			}
			
			
			/* Getting the config file to use 
			 * Note: we do not use class.getName() because the PreferencesSerivce  is declared as optional
			 */
			String configStr = System.getProperty("org.paxle.data.db.impl.CommandDB");
			if (configStr != null) {
				this.logger.info("Loading db configuration from '" + configStr + "' ...");
				config = new URL(configStr);				
			} else {						
				this.logger.info("Loading db configuration from /resources/hibernate/derby.cfg.xml ...");
				System.err.println("class-getResource: " + this.getClass().getResource("/resources/hibernate/derby.cfg.xml"));
				System.err.println("context-getEntry: " + context.getBundle().getEntry("/resources/hibernate/derby.cfg.xml"));
				System.err.println("context-getEntry: " + context.getBundle().getResource("/resources/hibernate/derby.cfg.xml"));
				 config = context.getBundle().getResource("/resources/hibernate/derby.cfg.xml");
//				config = new URL("bundle://15.0:1/resources/hibernate/derby.cfg.xml");
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
		
		// fill the crawler queue with URLs
		TextCommandReader fileReader = new TextCommandReader(this.getClass().getResourceAsStream("/resources/data.txt"));
		Hashtable<String,String> readerProps = new Hashtable<String, String>();
		readerProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");
		context.registerService(IDataProvider.class.getName(), fileReader, readerProps);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}

	private void classLoaderCheck() throws ClassNotFoundException {
		try {
			this.getClass().getClassLoader().loadClass("javax.sql.DataSource");
		} catch (ClassNotFoundException e) {
			throw new ExceptionInInitializerError("Please export 'javax.sql' via org.osgi.framework.system.packages.");
		}
	}
	
	private void pipeConnect(String from, String to) {
		this.logger.info(String.format("Create datapipe: %s -> %s",from,to));
		
		final DataPipe pipe = new DataPipe();
		pipe.setName(String.format("Datapipe: %s -> %s", from,to));
		final Hashtable<String,String> props = new Hashtable<String,String>();
		props.put(IDataConsumer.PROP_DATACONSUMER_ID, from);
		props.put(IDataProvider.PROP_DATAPROVIDER_ID, to);
		bc.registerService(new String[]{IDataConsumer.class.getName(),IDataProvider.class.getName()}, pipe, props);
	}
}