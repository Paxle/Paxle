package org.paxle.core.queue.impl;

import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.event.EventAdmin;

public abstract class AQueue<Data> extends ArrayBlockingQueue<Data> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * for logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());	
	
	/**
	 * Component to send events
	 */
	protected EventAdmin eventSerivce = null;
	
	public AQueue(int length) {		
		super(length);
	}	
	
	public void setEventService(EventAdmin eventService) {
		this.eventSerivce = eventService;
	}
}
