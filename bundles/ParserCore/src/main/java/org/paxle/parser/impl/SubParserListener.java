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

package org.paxle.parser.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.parser.ISubParser;

public class SubParserListener implements ServiceListener {

	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER = String.format("(&(%s=%s)(%s=*)(%s=*))",
			// we are only interested in ISubParsers
			Constants.OBJECTCLASS,ISubParser.class.getName(),
			// subparsers must have a mimetype property
			ISubParser.PROP_MIMETYPES,
			// supparsers must habe a service.pid
			Constants.SERVICE_PID
	);	
	
	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */	
	private SubParserManager subParserManager = null;
	
	public SubParserListener(SubParserManager subParserManager, BundleContext context) throws InvalidSyntaxException {
		this.subParserManager = subParserManager;
		
		ServiceReference[] services = context.getServiceReferences(null,FILTER);
		if (services != null)
			for (ServiceReference service : services)
				serviceChanged(service, ServiceEvent.REGISTERED);
	}
	
	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		// getting the service reference
		ServiceReference reference = event.getServiceReference();
		this.serviceChanged(reference, event.getType());
	}		
	
	private void serviceChanged(ServiceReference reference, int eventType) {
		if (reference == null) return;		
		
		if (eventType == ServiceEvent.REGISTERED) {
			// new service was installed
			this.subParserManager.addSubParser(reference);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			this.subParserManager.removeSubParser(reference);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}
}