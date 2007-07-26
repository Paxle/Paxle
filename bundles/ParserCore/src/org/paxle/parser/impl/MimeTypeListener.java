package org.paxle.parser.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.mimetype.IMimeTypeDetector;

public class MimeTypeListener implements ServiceListener {

	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static String FILTER = "(objectClass=" + IMimeTypeDetector.class.getName() +")";		
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;	
	
	private WorkerFactory workerFactory = null;
	
	public MimeTypeListener(WorkerFactory workerFactory, BundleContext context) {
		this.workerFactory = workerFactory;
		this.context = context;		
		
		ServiceReference reference = context.getServiceReference(IMimeTypeDetector.class.getName());
		if (reference != null) {
			// get the mimetype detector service
			IMimeTypeDetector mimeTypeDetector = (IMimeTypeDetector) this.context.getService(reference);
			
			// pass the service to the worker thread factory
			this.workerFactory.setMimeTypeDetector(mimeTypeDetector);			
		}
	}
	
	public void serviceChanged(ServiceEvent event) {
		ServiceReference reference = event.getServiceReference();
		
		int eventType = event.getType();
		if (eventType == ServiceEvent.REGISTERED) {			
			// get the mimetype detector service
			IMimeTypeDetector mimeTypeDetector = (IMimeTypeDetector) this.context.getService(reference);
			
			// pass the service to the worker thread factory
			this.workerFactory.setMimeTypeDetector(mimeTypeDetector);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			this.workerFactory.setMimeTypeDetector(null);
			this.context.ungetService(reference);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}	
	}
}
