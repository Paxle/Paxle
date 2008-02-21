package org.paxle.core.data;

/**
 * A {@link IDataSink data-sink} is filled ba a {@link IDataProvider data-provider}
 * with data.
 */
public interface IDataSink<Data> {
	public static final String PROP_DATASINK_ID = IDataSink.class.getName() + ".id";
	
	public void putData(Data data) throws Exception;
}
