package org.paxle.core;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;


public class MWComponentEvent {
	/* ======================================================================
	 * Event Topics
	 * ====================================================================== */
	public static final String TOPIC_ = MWComponentEvent.class.getName().replace('.', '/') + "/";
	
	public static final String TOPIC_ALL = TOPIC_ + "*";
	
	public static final String TOPIC_PAUSED = TOPIC_ + "PAUSED";
	
	public static final String TOPIC_RESUMED = TOPIC_ + "RESUMED";
	
	/* ======================================================================
	 * Event Properties
	 * ====================================================================== */
	public static final String PROP_COMPONENT_ID = "componentID";	
	
	public static Event createEvent(String componentID, String topic) {
		if (componentID == null) throw new NullPointerException("The component-ID is null");
		if (topic == null) throw new NullPointerException("No topic specified.");
		
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put(PROP_COMPONENT_ID, componentID);
		properties.put(EventConstants.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
		
		return new Event(topic,properties);
	}
}
