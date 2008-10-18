package org.paxle.core.queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.EventAdmin;

public abstract class AQueue<Data> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * for logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());	
	
	/**
	 * Component to send {@link org.osgi.service.event.Event events}
	 */
	protected EventAdmin eventService = null;
	
	/**
	 * An internal {@link BlockingQueue}
	 */
	protected final BlockingQueue<Data> queue;
	
	public AQueue(final int length) {
		this(length, false);
	}
	
	public AQueue(int length, final boolean limited) {
		this.queue = (limited) ? new ArrayBlockingQueue<Data>(length) : new LinkedBlockingQueue<Data>(length);
	}	
	
	public void setEventService(EventAdmin eventService) {
		this.eventService = eventService;
	}
	
	/**
	 * @return the number of elements in the queue
	 */
	public int size() {
		return this.queue.size();
	}
	
	/**
	 * @return the number of elements that this queue can ideally 
	 * accept without blocking.
	 */
	public int remainingCapacity() {
		return this.queue.remainingCapacity();
	}
	
	/**
	 * @return an array containing all of the elements in this queue
	 */
	public Object[] toArray() {
		return this.queue.toArray();
	}	
}
