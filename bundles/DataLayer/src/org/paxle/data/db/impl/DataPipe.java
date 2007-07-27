package org.paxle.data.db.impl;

import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.data.IDataSource;

/**
 * This pipe acts as {@link IDataConsumer data-consumer} and {@link IDataProvider data-provider}
 * and just copies data from a {@link IDataSource data-source} to a {@link IDataSink data-sink}
 */
public class DataPipe<Data> extends Thread implements IDataProvider<Data>, IDataConsumer<Data> {
	
	private IDataSink<Data> sink = null;
	private IDataSource<Data> source = null;
	
	public DataPipe() {
		this.start();
	}
	
	/**
	 * @see IDataProvider#setDataSink(IDataSink)
	 */
	public synchronized void setDataSink(IDataSink<Data> dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null-");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		this.sink = dataSink;
		this.notify();
	}

	/**
	 * @see IDataConsumer#setDataSource(IDataSource)
	 */
	public synchronized void setDataSource(IDataSource<Data> dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		this.source = dataSource;
		this.notify();
	}
	
	/**
	 * @see Thread#run()
	 */
	@Override
	public void run() {
		try {
			synchronized (this) {
				while ((this.sink == null) || (this.source == null)) this.wait();
			}
			
			while (true) {
				Data data = this.source.getData();
				this.sink.putData(data);
			}
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

}
