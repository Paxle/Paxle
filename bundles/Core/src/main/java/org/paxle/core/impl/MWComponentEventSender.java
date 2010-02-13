/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
