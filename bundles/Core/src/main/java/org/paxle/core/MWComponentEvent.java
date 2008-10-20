/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
