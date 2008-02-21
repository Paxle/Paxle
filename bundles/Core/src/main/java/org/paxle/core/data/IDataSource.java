package org.paxle.core.data;

/**
 * A {@link IDataSource data-source} is read by a {@link IDataConsumer data-consumer}
 */
public interface IDataSource<Data> {
	public static final String PROP_DATASOURCE_ID = IDataSource.class.getName() + ".id";	
	
	public Data getData() throws Exception;
}
