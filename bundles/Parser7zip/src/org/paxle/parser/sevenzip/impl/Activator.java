package org.paxle.parser.sevenzip.impl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.sevenzip.I7zipParser;

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
		ISubParser hp = new P7zipParser();
		Hashtable<String,String> props = new Hashtable<String,String>();
		List<String> mimeTypes = hp.getMimeTypes();
		StringBuilder sb = new StringBuilder(mimeTypes.size() * 20);
		Iterator<String> it = mimeTypes.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
			if (it.hasNext())
				sb.append(';');
		}
		props.put(ISubParser.PROP_MIMETYPES, sb.toString());
		bc.registerService(new String[]{ISubParser.class.getName(),I7zipParser.class.getName()},hp, props);
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}