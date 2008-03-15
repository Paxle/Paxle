package org.paxle.crawler.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.mimetype.IMimeTypeDetector;

public class DetectorListener implements ServiceListener {
	/**
	 * Interface-names of the {@link org.paxle.parser.ISubParser subparsers}
	 * currently registered to the system.
	 */
	private static final String SUBPARSER = "org.paxle.parser.ISubParser";
	
	/**
	 * Mimetypes supported by a {@link org.paxle.parser.ISubParser subparser}.
	 * @see {@link org.paxle.parser.ISubParser#MIMETYPES}
	 */
	private static final String SUBPARSER_MIMETYPES = "MimeTypes";

	/**
	 * The interfaces to listen for
	 */
	private static final HashSet<String> INTERFACES = new HashSet<String>(Arrays.asList(new String[]{
		ICharsetDetector.class.getName(),
		ICryptManager.class.getName(),
		IMimeTypeDetector.class.getName(),
		
		/* don't use the the .class directly otherwise we have an unwanted dependency */
		SUBPARSER
	}));

	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER;
	static {
		final StringBuilder sb = new StringBuilder("(|");
		for (String intrface : INTERFACES) sb.append(String.format("(%s=%s)",Constants.OBJECTCLASS,intrface));
		FILTER = sb.append(')').toString();
	}

	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;	

	private CrawlerContextLocal crawlerLocal = null;

	public DetectorListener(CrawlerContextLocal crawlerLocal, BundleContext context) throws InvalidSyntaxException {
		this.crawlerLocal = crawlerLocal;
		this.context = context;		

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

		// get the names of the registered interfaces 
		String[] interfaceNames = ((String[])reference.getProperty(Constants.OBJECTCLASS));

		// loop through the interfaces
		for (String interfaceName : interfaceNames) {
			if (!INTERFACES.contains(interfaceName)) continue;

			if (eventType == ServiceEvent.REGISTERED) {			
				// get the detector service
				Object detector = this.context.getService(reference);

				// pass it to the worker factory
				if (interfaceName.equals(ICharsetDetector.class.getName())) {
					this.crawlerLocal.setCharsetDetector((ICharsetDetector) detector);
				} else if (interfaceName.equals(ICryptManager.class.getName())) {
					this.crawlerLocal.setCryptManager((ICryptManager)detector);
				} else if (interfaceName.equals(IMimeTypeDetector.class.getName())) {
					this.crawlerLocal.setMimeTypeDetector((IMimeTypeDetector)detector);
				} else if (interfaceName.equals(SUBPARSER)) {
					String[] mimeTypes = this.getSubParserMimeTypes(reference);
					
					for (String mimeType : mimeTypes) {
						this.crawlerLocal.getSupportedMimeTypes().add(mimeType.trim());
					}
				}
			} else if (eventType == ServiceEvent.UNREGISTERING) {
				if (interfaceName.equals(ICharsetDetector.class.getName())) {
					this.crawlerLocal.setCharsetDetector(null);
				} else if (interfaceName.equals(ICryptManager.class.getName())) {
					this.crawlerLocal.setCryptManager(null);
				} else if (interfaceName.equals(IMimeTypeDetector.class.getName())) {
					this.crawlerLocal.setMimeTypeDetector(null);
				} else if (interfaceName.equals(SUBPARSER)) {
					String[] mimeTypes = this.getSubParserMimeTypes(reference);
					
					for (String mimeType : mimeTypes) {
						this.crawlerLocal.getSupportedMimeTypes().remove(mimeType.trim());
					}
				}
				this.context.ungetService(reference);
			} else if (eventType == ServiceEvent.MODIFIED) {
				// service properties have changed
			}	
		}
	}
	
	private String[] getSubParserMimeTypes(ServiceReference reference) {
		String[] mimeTypes = {};
		Object mimeTypesProp = reference.getProperty(SUBPARSER_MIMETYPES);
		if (mimeTypesProp instanceof String) mimeTypes = new String[]{(String)mimeTypesProp};
		else if (mimeTypesProp instanceof String[]) mimeTypes = (String[]) mimeTypesProp;
		return mimeTypes;
	}
}
