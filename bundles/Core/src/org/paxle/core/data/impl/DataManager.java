package org.paxle.core.data.impl;

import java.util.Hashtable;

import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataManager;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;

public class DataManager<Data> implements IDataManager {
	private Hashtable<String, IDataSource<Data>> dataSources = new Hashtable<String, IDataSource<Data>>();
	private Hashtable<String, IDataSink<Data>> dataSinks = new Hashtable<String, IDataSink<Data>>();
	private Hashtable<String, IDataProvider<Data>> dataProviders = new Hashtable<String, IDataProvider<Data>>();
	private Hashtable<String, IDataConsumer<Data>> dataConsumers = new Hashtable<String, IDataConsumer<Data>>();
	
	public void add(String ID, String interfaceName, Object service) {		
		if (ID == null) return;
		if (service == null) return;
		
		Hashtable table = null;
		
		// get the proper table
		if (interfaceName.equals(IDataSource.class.getName())) {
			table = this.dataSources;
			IDataConsumer<Data> consumer = this.dataConsumers.get(ID);
			if (consumer != null) {
				consumer.setDataSource((IDataSource<Data>) service);
			}
		} else if (interfaceName.equals(IDataSink.class.getName())) {
			table = this.dataSinks;
			IDataProvider<Data> provider = this.dataProviders.get(ID);
			if (provider != null) {
				provider.setDataSink((IDataSink<Data>)service);
			}
		} else if (interfaceName.equals(IDataProvider.class.getName())) {
			table = this.dataProviders;
			IDataSink<Data> sink = this.dataSinks.get(ID);
			if (sink != null) {
				((IDataProvider<Data>)service).setDataSink(sink);
			}
		} else if (interfaceName.equals(IDataConsumer.class.getName())) {
			table = this.dataConsumers;
			IDataSource<Data> source = this.dataSources.get(ID);
			if (source != null) {
				((IDataConsumer<Data>)service).setDataSource(source);
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
