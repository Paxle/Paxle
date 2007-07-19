package org.paxle.core.data.impl;

import java.util.Hashtable;

import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataManager;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;

public class DataManager implements IDataManager {
	private Hashtable<String, IDataSource> dataSources = new Hashtable<String, IDataSource>();
	private Hashtable<String, IDataSink> dataSinks = new Hashtable<String, IDataSink>();
	private Hashtable<String, IDataProvider> dataProviders = new Hashtable<String, IDataProvider>();
	private Hashtable<String, IDataConsumer> dataConsumers = new Hashtable<String, IDataConsumer>();
	
	public void add(String ID, String interfaceName, Object service) {		
		if (ID == null) return;
		if (service == null) return;
		
		Hashtable table = null;
		
		// get the proper table
		if (interfaceName.equals(IDataSource.class.getName())) {
			table = this.dataSources;
			IDataConsumer consumer = this.dataConsumers.get(ID);
			if (consumer != null) {
				consumer.setDataSource((IDataSource) service);
			}
		} else if (interfaceName.equals(IDataSink.class.getName())) {
			table = this.dataSinks;
			IDataProvider provider = this.dataProviders.get(ID);
			if (provider != null) {
				provider.setDataSink((IDataSink)service);
			}
		} else if (interfaceName.equals(IDataProvider.class.getName())) {
			table = this.dataProviders;
			IDataSink sink = this.dataSinks.get(ID);
			if (sink != null) {
				((IDataProvider)service).setDataSink(sink);
			}
		} else if (interfaceName.equals(IDataConsumer.class.getName())) {
			table = this.dataConsumers;
			IDataSource source = this.dataSources.get(ID);
			if (source != null) {
				((IDataConsumer)service).setDataSource(source);
			}
		}
		
		// insert object
		table.put(ID, service);
		
		// concatenate sources->consumers / sinks->providers
	}
	
	public void remove(String ID) {
		// TODO
		throw new RuntimeException("Not implemented!");
	}
}
