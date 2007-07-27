package org.paxle.parser.gzip.impl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.gzip.IGzipParser;

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
		final ISubParser sp = new GzipParser();
		final List<String> mimeTypes = sp.getMimeTypes();
		StringBuilder sb = new StringBuilder(mimeTypes.size() * 20);
		Iterator<String> it = mimeTypes.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext())
				sb.append(';');
		}
		final Hashtable<String,String> props = new Hashtable<String,String>();
		props.put(ISubParser.PROP_MIMETYPES, sb.toString());
		context.registerService(new String[]{ISubParser.class.getName(),IGzipParser.class.getName()}, sp, props);
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}