package org.paxle.data.impl;

import java.net.MalformedURLException;
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
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.core.queue.ICommandTracker;
import org.paxle.data.db.ICommandDB;
import org.paxle.data.db.impl.CommandDB;
import org.paxle.data.db.impl.CommandProfileDB;
import org.paxle.data.db.impl.CommandProfileFilter;
import org.paxle.data.db.impl.UrlExtractorFilter;

import com.sun.org.apache.xerces.internal.util.URI;

public class Activator implements BundleActivator {

	/**
	 * Logger
	 */
	private Log logger = null;
	
	/**
	 * A DB to store {@link ICommand commands}
	 */
	private CommandDB commandDB = null;
	
	/**
	 * A DB to store {@link ICommandProfile command-profiles}
	 */
	private CommandProfileDB profileDB = null;
	
	/**
	 * A {@link org.paxle.core.filter.IFilter} used to extract {@link URI} from 
	 * the {@link org.paxle.core.doc.IParserDocument} and to store them into
	 * the {@link #commandDB command-DB}
	 */
	private UrlExtractorFilter urlExtractor = null;
	
	private CommandProfileFilter profileFilter = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext context) throws Exception {
		// init logger
		this.logger = LogFactory.getLog(this.getClass());

		/* =========================================================
		 * INIT DATABASES
		 * ========================================================= */		
		// determine the hibernate configuration file to use
		URL hibernateConfigFile = this.getConfigURL(context);

		// init the command-DB
		this.createAndRegisterCommandDB(hibernateConfigFile, context);

		// init the command-profile-DB
		this.createAndRegisterProfileDB(hibernateConfigFile, context);

		/* =========================================================
		 * INIT COMMAND-FILTERS
		 * ========================================================= */		
		this.createAndRegisterUrlExtractorFilter(context);
		this.createAndRegisterCommandProfileFilter(context);

		/* =========================================================
		 * DATABASE-STARTUP
		 * ========================================================= */
		this.commandDB.start();
		
		/*
			// fill the crawler queue with URLs
			TextCommandReader fileReader = new TextCommandReader(this.getClass().getResourceAsStream("/resources/data.txt"));
			Hashtable<String,String> readerProps = new Hashtable<String, String>();
			readerProps.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");
			context.registerService(IDataProvider.class.getName(), fileReader, readerProps);
		*/
	}
	
	@SuppressWarnings("unchecked")
	private URL getConfigURL(BundleContext context) throws MalformedURLException {
		URL config = null;
		this.logger.info("Trying to find db config files ...");

		// we may have multiple datalayer-fragment-bundles installed
		Enumeration<URL> configFileEnum = context.getBundle().findEntries("/resources/hibernate/", "*.cfg.xml", true);
		if (configFileEnum != null) {				
			ArrayList<URL> configFiles = Collections.list(configFileEnum);
			this.logger.info(String.format("%d config-files found.", Integer.valueOf(configFiles.size())));
		} else {
			this.logger.info("No config files found");
		}


		/* 
		 * Getting the config file to use 
		 */
		String configStr = System.getProperty("org.paxle.data.db.impl.CommandDB");
		if (configStr != null) {
			this.logger.info("Loading db configuration from '" + configStr + "' ...");
			config = new URL(configStr);				
		} else {						
			this.logger.info("Loading db configuration from /resources/hibernate/derby.cfg.xml ...");
//			System.err.println("class-getResource: " + this.getClass().getResource("/resources/hibernate/derby.cfg.xml"));
//			System.err.println("context-getEntry: " + context.getBundle().getEntry("/resources/hibernate/derby.cfg.xml"));
//			System.err.println("context-getEntry: " + context.getBundle().getResource("/resources/hibernate/derby.cfg.xml"));
			config = context.getBundle().getResource("/resources/hibernate/derby.cfg.xml");
//			config = new URL("bundle://15.0:1/resources/hibernate/derby.cfg.xml");
		}		
		
		return config;
	}
	
	@SuppressWarnings("unchecked")
	private void createAndRegisterCommandDB(URL hibernateConfigFile, BundleContext context) {
        // getting the Event-Admin service
        ServiceReference commandTrackerRef = context.getServiceReference(ICommandTracker.class.getName());
        ICommandTracker commandTracker = (commandTrackerRef == null) ? null :  (ICommandTracker) context.getService(commandTrackerRef);
        if (commandTracker == null) {
        	this.logger.warn("No CommandTracker-service found. Command-tracking will not work.");
        }		
		
		// getting the mapping files to use
		Enumeration<URL> mappingFileEnum = context.getBundle().findEntries("/resources/hibernate/mapping/command/", "command.hbm.xml", true);
		ArrayList<URL> mappings = Collections.list(mappingFileEnum);

		// init command DB
		this.commandDB = new CommandDB(hibernateConfigFile, mappings, commandTracker);		
		
		final Hashtable<String,Object> props = new Hashtable<String,Object>();
//		props.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
		props.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.crawler.sink");		
		props.put(EventConstants.EVENT_TOPIC, new String[]{CommandEvent.TOPIC_OID_REQUIRED});
		context.registerService(new String[]{
				IDataConsumer.class.getName(),
				IDataProvider.class.getName(),
				ICommandDB.class.getName(),
				EventHandler.class.getName()
		}, this.commandDB, props);		
	}
	
	@SuppressWarnings("unchecked")
	private void createAndRegisterProfileDB(URL hibernateConfigFile, BundleContext context) {
		// getting the mapping files to use
		Enumeration<URL> mappingFileEnum = context.getBundle().findEntries("/resources/hibernate/mapping/profile/", "*.hbm.xml", true);
		ArrayList<URL> mappings = Collections.list(mappingFileEnum);
		
		// create the profile-DB
		this.profileDB = new CommandProfileDB(hibernateConfigFile, mappings);
		
		// register it to the framework
		context.registerService(ICommandProfileManager.class.getName(), this.profileDB, null);
	}

	private void createAndRegisterUrlExtractorFilter(BundleContext context) {		
		Hashtable<String, String[]> urlExtractorFilterProps = new Hashtable<String, String[]>();
		urlExtractorFilterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{
				String.format("org.paxle.parser.out; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.valueOf(Integer.MAX_VALUE))
		});
		
		this.urlExtractor = new UrlExtractorFilter(this.commandDB);
		context.registerService(IFilter.class.getName(), this.urlExtractor, urlExtractorFilterProps);
	}
	
	private void createAndRegisterCommandProfileFilter(BundleContext context) {		
		Hashtable<String, String[]> profileFilterProps = new Hashtable<String, String[]>();
		profileFilterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{
				String.format("org.paxle.crawler.in; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.valueOf(Integer.MIN_VALUE)),
				String.format("org.paxle.parser.out; %s=%d",IFilter.PROP_FILTER_TARGET_POSITION,Integer.valueOf(Integer.MAX_VALUE-1))
		});
		
		this.profileFilter = new CommandProfileFilter(this.profileDB);
		context.registerService(IFilter.class.getName(), this.profileFilter, profileFilterProps);	
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// shutdown URL-extractor
		if (this.urlExtractor != null) {
			this.urlExtractor.terminate();
		}
		
		// shutdown command DB
		if (this.commandDB != null) {
			this.commandDB.close();
		}		
	}

}
