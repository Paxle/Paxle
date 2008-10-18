package org.paxle.parser.html.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.html.IHtmlParser;

public class Activator implements BundleActivator {
	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */	
	public static BundleContext bc;
	
	private HtmlParser parser = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext context) throws Exception {
		bc = context;
		parser = new HtmlParser();
		Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		List<String> mimeTypes = parser.getMimeTypes();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		bc.registerService(new String[]{ISubParser.class.getName(),IHtmlParser.class.getName()}, parser, props);
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		if (parser != null) {
			parser.close();
			parser = null;
		}
		bc = null;
	}
}