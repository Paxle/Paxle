package org.paxle.core.data;

/**
 *	This class actively "pulls" data frin a {@link IDataSource data-source}.
 */
public interface IDataConsumer {
	public static final String PROP_DATACONSUMER_ID = IDataConsumer.class.getName() + ".id";	
	
	/**
	 * Assign a {@link IDataSource data-source} to the {@link IDataConsumer data-consumer}
	 * @param dataSource
	 */
	public void setDataSource(IDataSource dataSource);
}
