
package org.paxle.parser.xbel.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;

public class Activator implements BundleActivator {	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		final ISubParser sp = new XbelParser();
		final List<String> mimeTypes = sp.getMimeTypes();
		
		// set service properties
		Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		
		// register service
		context.registerService(new String[]{ISubParser.class.getName(), XbelParser.class.getName()}, sp, props);
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
	}
}
