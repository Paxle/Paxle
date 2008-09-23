package org.paxle.core.data;

/**
 * A {@link IDataSink data-sink} is filled by a {@link IDataProvider data-provider}
 * with data.
 * 
 * A {@link IDataSink data-sink} may be implemented as a simple {@link java.util.concurrent.BlockingQueue} but
 * could also be implemented as a network-connection to a remote host.
 */
public interface IDataSink<Data> {
	public static final String PROP_DATASINK_ID = IDataSink.class.getName() + ".id";
	
	/**
	 * Writes the specified element to the sink, waiting if necessary for a new element 
	 * to become available.
	 * 
	 * @param data
	 * @throws Exception
	 */
	public void putData(Data data) throws Exception;
	
	/**
	 * Writes the specified element to the sink if possible, 
	 * returning immediately if the sink does not accept new data at the moment.
	 * 
	 * @param data
	 * @return <tt>true</tt> if it was possible to write the element to
     *         the sink, else <tt>false</tt>
	 * @throws Exception
	 * 
	 * @since 0.1.4
	 */
	public boolean offerData(Data data) throws Exception;
	
	/**
	 * @return the number of elements that are accepted by the {@link IDataSink data-sink} without blocking 
	 * 		   or <code>-1</code> if the used {@link IDataSink data-sink}-implementation does not support
	 * 		   querying the remaining capacity.
	 * 
	 * @throws Exception
	 * @since 0.1.4
	 */
	public int freeCapacity() throws Exception;
	
	/**
	 * @return <code>true</code> if the {@link IDataSink data-sink}-implementation supports determinig the
	 * free capacity via function-call {@link #freeCapacity()}
	 * 
	 * @since 0.1.4
	 */
	public boolean freeCapacitySupported();
}
