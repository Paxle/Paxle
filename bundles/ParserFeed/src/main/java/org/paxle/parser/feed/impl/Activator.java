
package org.paxle.parser.feed.impl;

import java.util.Hashtable;
import java.util.List;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.feed.IFeedParser;

public class Activator implements BundleActivator {
	
	public void start(BundleContext context) throws Exception {
		final ISubParser sp = new FeedParser();
		final Hashtable<String,String[]> props = new Hashtable<String,String[]>();
		final List<String> mimeTypes = sp.getMimeTypes();
		props.put(ISubParser.PROP_MIMETYPES, mimeTypes.toArray(new String[mimeTypes.size()]));
		context.registerService(new String[] { ISubParser.class.getName(), IFeedParser.class.getName() }, sp, props);
	}
	
	public void stop(BundleContext context) throws Exception {
	}
}
