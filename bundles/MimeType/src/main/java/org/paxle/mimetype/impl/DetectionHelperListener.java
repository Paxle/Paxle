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

package org.paxle.mimetype.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.mimetype.IDetectionHelper;

public class DetectionHelperListener implements ServiceListener {
	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER = String.format("(%s=%s)",Constants.OBJECTCLASS,IDetectionHelper.class.getName());
		
	private MimeTypeDetector detector = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;	
	
	public DetectionHelperListener(MimeTypeDetector manager, BundleContext context) throws InvalidSyntaxException {
		this.context = context;
		this.detector = manager;
		
		ServiceReference[] services = context.getServiceReferences(null,FILTER);
		if (services != null) for (ServiceReference service : services) serviceChanged(service, ServiceEvent.REGISTERED);	
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
		
		// the protocol supported by the detected sub-crawler
		Object mimeTypes = reference.getProperty(IDetectionHelper.PROP_MIMETYPES);				
				
		if (eventType == ServiceEvent.REGISTERED) {			
			// a reference to the service
			IDetectionHelper detector = (IDetectionHelper) context.getService(reference);
			
			// new service was installed
			if (mimeTypes instanceof String[])
				for (String mimeType : (String[]) mimeTypes) this.detector.addDetectionHelper(mimeType,detector);
			else if (mimeTypes instanceof String) 
				this.detector.addDetectionHelper((String)mimeTypes,detector);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			if (mimeTypes instanceof String[])
				for (String mimeType : (String[]) mimeTypes) this.detector.removeDetectionHelper(mimeType);
			else if (mimeTypes instanceof String) 
				this.detector.removeDetectionHelper((String)mimeTypes);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}	
}
