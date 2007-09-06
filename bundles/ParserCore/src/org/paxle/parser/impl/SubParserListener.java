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
	public static final String FILTER = String.format("(&(%s=%s)(%s=*))",
			Constants.OBJECTCLASS,ISubParser.class.getName(),ISubParser.PROP_MIMETYPES);	
	
	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */	
	private SubParserManager subParserManager = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;
	
	public SubParserListener(SubParserManager subParserManager, BundleContext context) throws InvalidSyntaxException {
		this.context = context;
		this.subParserManager = subParserManager;
		
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
		Object mimeTypes = reference.getProperty(ISubParser.PROP_MIMETYPES);		
		
		if (eventType == ServiceEvent.REGISTERED) {			
			// a reference to the service
			ISubParser subParser = (ISubParser) this.context.getService(reference);				
			
			// new service was installed
			if (mimeTypes instanceof String[])
				this.subParserManager.addSubParser((String[])mimeTypes, subParser);
			else if (mimeTypes instanceof String) 
				this.subParserManager.addSubParser((String)mimeTypes, subParser);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			if (mimeTypes instanceof String[])
				this.subParserManager.removeSubParser((String[])mimeTypes);
			else if (mimeTypes instanceof String) 
				this.subParserManager.removeSubParser((String)mimeTypes);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}
}