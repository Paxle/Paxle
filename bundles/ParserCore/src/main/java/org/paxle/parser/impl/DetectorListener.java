/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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

import java.util.Arrays;
import java.util.HashSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;

public class DetectorListener implements ServiceListener {

	/**
	 * The interfaces to listen for
	 */
	private static final String[] INTERFACES = new String[]{
		IMimeTypeDetector.class.getName(),
		ICharsetDetector.class.getName(),
		ITempFileManager.class.getName(),
		IReferenceNormalizer.class.getName()
	};
	
	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER;
	static {
		final StringBuilder sb = new StringBuilder();
		sb.append("(|");
		for (final String str : INTERFACES)
			sb.append('(').append(Constants.OBJECTCLASS).append('=').append(str).append(')');
		FILTER = sb.append(')').toString();
	}
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;	
	
	private WorkerFactory workerFactory = null;
	
	public DetectorListener(WorkerFactory workerFactory, BundleContext context) {
		this.workerFactory = workerFactory;
		this.context = context;		
		
		for (String interfaceName : INTERFACES) {
			ServiceReference reference = context.getServiceReference(interfaceName);
			this.serviceChanged(reference, ServiceEvent.REGISTERED);
		}
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
		
		// get the names of the registered interfaces 
		HashSet<String> interfaces = this.arrayToSet((String[])reference.getProperty(Constants.OBJECTCLASS));		
		
		if (eventType == ServiceEvent.REGISTERED) {			
			// get the detector service
			Object detector = this.context.getService(reference);

			// pass it to the worker factory
			if (interfaces.contains(IMimeTypeDetector.class.getName())) {
				this.workerFactory.setMimeTypeDetector((IMimeTypeDetector) detector);
			}
			if (interfaces.contains(ICharsetDetector.class.getName())) {
				this.workerFactory.setCharsetDetector((ICharsetDetector) detector);
			}
			if (interfaces.contains(ITempFileManager.class.getName())) {
				this.workerFactory.setTempFileManager((ITempFileManager)detector);
			}
			if (interfaces.contains(IReferenceNormalizer.class.getName())) {
				this.workerFactory.setReferenceNormalizer((IReferenceNormalizer)detector);
			}
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			if (interfaces.contains(IMimeTypeDetector.class.getName())) {
				this.workerFactory.setMimeTypeDetector(null);
			}
			if (interfaces.contains(ICharsetDetector.class.getName())) {
				this.workerFactory.setCharsetDetector(null);
			}
			if (interfaces.contains(ITempFileManager.class.getName())) {
				this.workerFactory.setTempFileManager(null);
			}
			if (interfaces.contains(IReferenceNormalizer.class.getName())) {
				this.workerFactory.setReferenceNormalizer(null);
			}
			this.context.ungetService(reference);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}	
	}
	
	private HashSet<String> arrayToSet(String[] interfaces) {
		HashSet<String> interfaceSet = new HashSet<String>();
		interfaceSet.addAll(Arrays.asList(interfaces));
		return interfaceSet;
	}
}
