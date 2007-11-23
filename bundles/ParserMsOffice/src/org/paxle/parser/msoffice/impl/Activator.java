package org.paxle.parser.msoffice.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.msoffice.IMsPowerpointParser;
import org.paxle.parser.msoffice.IMsVisioParser;
import org.paxle.parser.msoffice.IMsWordParser;

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
		
		Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		List<String> mimeTypes = null;
		ISubParser parser = null;
		
		// MS Word parser
		parser = new MsWordParser();
		mimeTypes = parser.getMimeTypes();
		props.clear();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IMsWordParser.class.getName()}, parser, props);
		
		// MS Powerpoint parser
		parser = new MsPowerpointParser();
		mimeTypes = parser.getMimeTypes();
		props.clear();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IMsPowerpointParser.class.getName()}, parser, props);	
		
		// MS Visio parser
		parser = new MsVisioParser();
		mimeTypes = parser.getMimeTypes();
		props.clear();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IMsVisioParser.class.getName()}, parser, props);			
	}


	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}