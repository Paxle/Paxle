package org.paxle.parser.bzip2.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.bzip2.IBzip2Parser;

public class Activator implements BundleActivator {
	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */	
	public static BundleContext bc;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		final ISubParser sp = new Bzip2Parser();
		Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		List<String> mimeTypes = sp.getMimeTypes();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		context.registerService(new String[]{ISubParser.class.getName(),IBzip2Parser.class.getName()}, sp, props);
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}