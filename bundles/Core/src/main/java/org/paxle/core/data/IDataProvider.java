package org.paxle.core.data;

/**
 * This class "pushes" data actively into a {@link IDataSink data-sink}.
 */
public interface IDataProvider<Data> {
	public static final String PROP_DATAPROVIDER_ID = IDataProvider.class.getName() + ".id";	
	
	/**
	 * Assign a {@link IDataSink data-sink} to the {@link IDataProvider data-provider}.
	 * @param dataSource
	 */	
	public void setDataSink(IDataSink<Data> dataSink);
}
