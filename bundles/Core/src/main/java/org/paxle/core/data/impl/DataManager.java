package org.paxle.core.data.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataManager;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;

public class DataManager<Data> implements IDataManager {
	// TODO: we should make this configurable via CM
	private static String[][] DATAPIPES = new String[][]{
		{"org.paxle.crawler.source", "org.paxle.parser.sink"},
		{"org.paxle.parser.source", "org.paxle.indexer.sink"}
	};	
	
	private ArrayList<DataPipe<?>> dataPipes = new ArrayList<DataPipe<?>>();
	private Hashtable<String, IDataSource<Data>> dataSources = new Hashtable<String, IDataSource<Data>>();
	private Hashtable<String, IDataSink<Data>> dataSinks = new Hashtable<String, IDataSink<Data>>();
	private Hashtable<String, List<IDataProvider<Data>>> dataProviders = new Hashtable<String, List<IDataProvider<Data>>>();
	private Hashtable<String, List<IDataConsumer<Data>>> dataConsumers = new Hashtable<String, List<IDataConsumer<Data>>>();
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	public DataManager() {
		this.logger.info("Initializing pipes ...");
		
		// init data pipes
		// TODO: this should be configurable via CM
		for (String[] datapipe : DATAPIPES) {
			this.pipeConnect(datapipe[0], datapipe[1]);
		}
	}
	
	public void close() throws InterruptedException {
		// shutdown pipes
		for (DataPipe<?> pipe : this.dataPipes) {
			pipe.terminate();
		}
		this.dataPipes.clear();
	}
	
	@SuppressWarnings("unchecked")
	public void add(String ID, String interfaceName, Object service) {		
		if (ID == null) return;
		if (service == null) return;
		
		if (interfaceName.equals(IDataSource.class.getName())) {
			/* ================================================================
			 * DATA SOURCES
			 * ================================================================ */
			
			logger.debug("DataSource '" + service + "' registering as '" + ID + "'");
			
			// ensure that we have only one data-source with the given ID
			if (this.dataSources.contains(ID)) {
				throw new IllegalArgumentException("The DataSource ID must be unique.");
			}
			
			// determine if there are consumers waiting for this source
			List<IDataConsumer<Data>> consumers = this.dataConsumers.get(ID);
			if (consumers != null) {
				for (IDataConsumer<Data> consumer : consumers) {
					consumer.setDataSource((IDataSource<Data>) service);
					logger.debug("Set DataSource for DataConsumer '" + consumer + "': " + service);
				}
			}			
			
			// add the data-source into our list
			this.dataSources.put(ID, (IDataSource<Data>) service);
			
		} else if (interfaceName.equals(IDataSink.class.getName())) {
			/* ================================================================
			 * DATA SINKS
			 * ================================================================ */		
			
			logger.debug("DataSink '" + service + "' registering as '" + ID + "'");	
			
			// ensure that we have only one data-sink with the given ID
			if (this.dataSinks.contains(ID)) {
				throw new IllegalArgumentException("The DataSink ID must be unique.");
			}
			
			// determine if there are data-providers waiting for this sink
			List<IDataProvider<Data>> providers = this.dataProviders.get(ID);
			if (providers != null) {
				for (IDataProvider<Data> provider : providers) {
					provider.setDataSink((IDataSink<Data>)service);
					logger.debug("Set DataSink for DataProvider '" + provider + "': " + service);
				}
			}
			
			// add the sink to our list
			this.dataSinks.put(ID, (IDataSink<Data>)service);
			
		} else if (interfaceName.equals(IDataProvider.class.getName())) {
			/* ================================================================
			 * DATA PROVIDERS
			 * ================================================================ */			
			
			logger.debug("DataProvider '" + service + "' registering as '" + ID + "'");
			
			// determine if the data-sink that is required by this provider is already registered
			IDataSink<Data> sink = this.dataSinks.get(ID);
			if (sink != null) {
				((IDataProvider<Data>)service).setDataSink(sink);
				logger.debug("Set DataSink for DataProvider '" + service + "': " + sink);
			}
			
			// add the provider into our list
			List<IDataProvider<Data>> providerList = this.dataProviders.get(ID);
			if (providerList == null) this.dataProviders.put(ID, providerList = new ArrayList<IDataProvider<Data>>());
			providerList.add((IDataProvider<Data>)service);			
			
		} else if (interfaceName.equals(IDataConsumer.class.getName())) {
			/* ================================================================
			 * DATA CONSUMER
			 * ================================================================ */			
			
			logger.debug("DataConsumer '" + service + "' registering as '" + ID + "'");
			
			// determine if the data-source that is required by this consumer is already registered
			IDataSource<Data> source = this.dataSources.get(ID);
			if (source != null) {
				((IDataConsumer<Data>)service).setDataSource(source);
				logger.debug("Set DataSource for DataConsumer '" + service + "': " + source);
			}
			
			// add the consumer into our list
			List<IDataConsumer<Data>> consumerList = this.dataConsumers.get(ID);
			if (consumerList == null) this.dataConsumers.put(ID, consumerList = new ArrayList<IDataConsumer<Data>>());
			consumerList.add((IDataConsumer<Data>)service);
		} else {
			throw new IllegalArgumentException("Unsupported interface detected: " + interfaceName);
		}
	}
	
	public void remove(String ID) {
		this.logger.error(String.format("Unable to remove %s. NOT IMPLEMENTED!",ID));
	}
	
	
	public void pipeConnect(String from, String to) {
		this.logger.info(String.format("Create datapipe: %s -> %s",from,to));
		
		// create pipe
		final DataPipe<Data> pipe = new DataPipe<Data>();
		pipe.setName(String.format("Datapipe: %s -> %s", from,to));
		this.dataPipes.add(pipe);
		
		// registering the pipes
		this.add(from, IDataConsumer.class.getName(), pipe);
		this.add(to, IDataProvider.class.getName(), pipe);
	}	
}
