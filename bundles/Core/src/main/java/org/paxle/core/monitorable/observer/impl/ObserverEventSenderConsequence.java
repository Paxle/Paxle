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

package org.paxle.core.monitorable.observer.impl;

import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.paxle.core.monitorable.observer.IObserverConsequence;

public class ObserverEventSenderConsequence implements IObserverConsequence {
	private static final String EVENT_TOPIC = "org/paxle/monitorable/observer";
	
	/**
	 * OSGi Event-admin service
	 */
	private EventAdmin eventAdmin;
	
	private boolean sendAsync = true;
	
	private Hashtable<String, Object> eventProperties;
	
	public ObserverEventSenderConsequence(BundleContext bc, Hashtable<String, Object> eventProperties) throws InvalidSyntaxException {
		this(bc, eventProperties, true);
	}
	
	public ObserverEventSenderConsequence(BundleContext bc, Hashtable<String, Object> eventProperties, boolean sendAsync) throws InvalidSyntaxException {
		this.eventProperties = eventProperties;
		this.sendAsync = sendAsync;
		
		// getting event sender
		ServiceReference evnetAdminRef = bc.getServiceReference(EventAdmin.class.getName());
		this.eventAdmin = (EventAdmin) bc.getService(evnetAdminRef);		
	}
	
	@SuppressWarnings("unchecked")
	public void triggerAction(Dictionary<String, Object> currentState) {
		Dictionary<String, Object> props = (Dictionary<String, Object>) this.eventProperties.clone();
		// props.put("mon.observer.state", new Hashtable<String, Object>(currentState));
		props.put("mon.observer.date", new Date());
		
		Event outEvent = new Event(EVENT_TOPIC,props);
		if (this.sendAsync) {
			this.eventAdmin.postEvent(outEvent);
		} else {
			this.eventAdmin.sendEvent(outEvent);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("event[")
		   .append(EVENT_TOPIC)
		   .append(":")
		   .append(this.eventProperties.toString())
		   .append("]");
		
		return buf.toString();
	}
}
