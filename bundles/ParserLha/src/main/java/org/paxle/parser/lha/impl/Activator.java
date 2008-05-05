
package org.paxle.parser.lha.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.lha.ILhaParser;

public class Activator implements BundleActivator {
	
	public void start(BundleContext context) throws Exception {
		final ILhaParser sp = new LhaParser();
		Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		List<String> mimeTypes = sp.getMimeTypes();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		context.registerService(new String[]{ISubParser.class.getName(),ILhaParser.class.getName()}, sp, props);
	}
	
	public void stop(BundleContext context) throws Exception {
	}
}
