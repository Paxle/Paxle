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
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.data.db.ICommandDB;
import org.paxle.data.db.impl.CommandDB;
import org.paxle.data.db.impl.CommandProfileFilter;
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
	
	private CommandDB commandDB = null;
	
	private ArrayList<DataPipe<?>> pipes = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		bc = context;
		this.pipes = new ArrayList<DataPipe<?>>();

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

        // getting the Event-Admin service
        ServiceReference commandTrackerRef = bc.getServiceReference(ICommandTracker.class.getName());
        ICommandTracker commandTracker = (commandTrackerRef == null) ? null :  (ICommandTracker) bc.getService(commandTrackerRef);
        if (commandTracker == null) {
        	this.logger.warn("No CommandTracker-service found. Command-tracking will not work.");
        }
		
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
			this.commandDB = new CommandDB(config, mappings, commandTracker);		

			final Hashtable<String,Object> props = new Hashtable<String,Object>();
//			props.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
			props.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");		
			props.put(EventConstants.EVENT_TOPIC, new String[]{CommandEvent.TOPIC_OID_REQUIRED});
			bc.registerService(new String[]{
					IDataConsumer.class.getName(),
					IDataProvider.class.getName(),
					ICommandDB.class.getName(),
					ICommandProfileManager.class.getName(),
					EventHandler.class.getName()
			}, commandDB, props);

			/* =====================================================
			 * Register filters
			 * ===================================================== */
			
			// command-extraction filter
			Hashtable<String, String[]> urlExtractorFilterProps = new Hashtable<String, String[]>();
			urlExtractorFilterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{
					String.format("org.paxle.parser.out; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.MAX_VALUE)
			});
			bc.registerService(IFilter.class.getName(), new UrlExtractorFilter(commandDB), urlExtractorFilterProps);	
			
			// command-profile filter
			Hashtable<String, String[]> profileFilterProps = new Hashtable<String, String[]>();
			profileFilterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{
					String.format("org.paxle.crawler.in; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.MIN_VALUE),
					String.format("org.paxle.parser.out; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.MAX_VALUE-1)
			});
			bc.registerService(IFilter.class.getName(), new CommandProfileFilter(commandDB), profileFilterProps);	

			// start the commandDB
			commandDB.start();
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
		// shutdown command DB
		if (this.commandDB != null) {
			this.commandDB.close();
		}
		
		// shutdown pipes
		for (DataPipe<?> pipe : this.pipes) {
			pipe.terminate();
		}
		
		// release bundle context
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
		
		this.pipes.add(pipe);
	}
}
