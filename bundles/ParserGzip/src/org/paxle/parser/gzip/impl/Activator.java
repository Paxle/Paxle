package org.paxle.parser.gzip.impl;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;

public class Activator implements BundleActivator {
	
	public static BundleContext bc;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
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
		context.registerService(ISubParser.class.getName(), sp, props);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}