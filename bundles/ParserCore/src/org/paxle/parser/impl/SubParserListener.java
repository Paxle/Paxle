package org.paxle.parser.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.parser.ISubParser;

public class SubParserListener implements ServiceListener {

	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static String FILTER = "(& (objectClass=" + ISubParser.class.getName () +") "+
	 								 "(" + ISubParser.PROP_MIMETYPES + "=*))";	
	
	/**
	 * A class to manage {@link ISubParser sub-parsers}
	 */	
	private SubParserManager subParserManager = null;
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;
	
	public SubParserListener(SubParserManager subParserManager, BundleContext context) {
		this.context = context;
		this.subParserManager = subParserManager;
		try {
			ServiceReference[] parserRefs = context.getServiceReferences(ISubParser.class.getName(),"(" + ISubParser.PROP_MIMETYPES + "=*)");
			if (parserRefs == null) return;
			for (ServiceReference ref : parserRefs) {
				// the protocol supported by the detected sub-crawler
				String mimeTypes = (String) ref.getProperty(ISubParser.PROP_MIMETYPES);
				
				// a reference to the service
				ISubParser subParser = (ISubParser) this.context.getService(ref);					
				
				// pass the newly detected sub-parser to the manager
				this.subParserManager.addSubParser(mimeTypes, subParser);
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		ServiceReference reference = event.getServiceReference();
				
		// the protocol supported by the detected sub-crawler
		String mimeTypes = (String) reference.getProperty(ISubParser.PROP_MIMETYPES);	
		
		int eventType = event.getType();
		if (eventType == ServiceEvent.REGISTERED) {			
			// a reference to the service
			ISubParser subParser = (ISubParser) this.context.getService(reference);				
			
			// new service was installed
			this.subParserManager.addSubParser(mimeTypes, subParser);
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			this.subParserManager.removeSubParser(mimeTypes);
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}		
	}
}