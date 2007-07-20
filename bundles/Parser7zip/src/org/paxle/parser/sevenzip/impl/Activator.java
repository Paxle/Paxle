package org.paxle.parser.sevenzip.impl;

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
		bc.registerService(ISubParser.class.getName(), hp, props);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}