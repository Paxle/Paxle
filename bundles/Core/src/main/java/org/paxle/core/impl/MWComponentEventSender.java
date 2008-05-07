package org.paxle.core.impl;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.paxle.core.MWComponentEvent;

public class MWComponentEventSender {
	private final ServiceTracker eventServiceTracker;
	
	public MWComponentEventSender(ServiceTracker eventServiceTracker) {
		this.eventServiceTracker = eventServiceTracker;
	}
	
	public void sendPausedEvent(String componentID) {
		this.sendEvent(componentID, MWComponentEvent.TOPIC_PAUSED);
	}
	
	public void sendResumedEvent(String componentID) {
		this.sendEvent(componentID, MWComponentEvent.TOPIC_RESUMED);
	}
	
	private void sendEvent(String componentID, String topic) {
		EventAdmin eventAdmin = (EventAdmin) this.eventServiceTracker.getService();
		if (eventAdmin == null) return;
		
		Event event = MWComponentEvent.createEvent(componentID, topic);
		eventAdmin.postEvent(event);
	}
}
