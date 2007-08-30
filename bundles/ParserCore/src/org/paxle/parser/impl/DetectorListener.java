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

public class DetectorListener implements ServiceListener {

	/**
	 * The interfaces to listen for
	 */
	private static String[] INTERFACES = new String[]{
		IMimeTypeDetector.class.getName(),
		ICharsetDetector.class.getName(),
		ITempFileManager.class.getName()
	};
	
	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER = String.format("(|(objectClass=%s)(objectClass=%s)(objectClass=%s))", 
			INTERFACES[0],
			INTERFACES[1],
			INTERFACES[2]
	);
	
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
			} else if (interfaces.contains(ICharsetDetector.class.getName())) {
				this.workerFactory.setCharsetDetector((ICharsetDetector) detector);
			} else if (interfaces.contains(ITempFileManager.class.getName())) {
				this.workerFactory.setTempFileManager((ITempFileManager)detector);
			}
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			if (interfaces.contains(IMimeTypeDetector.class.getName())) {
				this.workerFactory.setMimeTypeDetector(null);
			} else if (interfaces.contains(ICharsetDetector.class.getName())) {
				this.workerFactory.setCharsetDetector(null);
			} else if (interfaces.contains(ITempFileManager.class.getName())) {
				this.workerFactory.setTempFileManager(null);
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
