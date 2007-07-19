package org.paxle.data.db.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataProvider;

public class Activator implements BundleActivator {  
	public void start(BundleContext context) throws Exception {
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */
		// this pipe connects the Crawler-Outqueue with the Parser-InQueue
		DataPipe pipe = new DataPipe();
		Hashtable<String,String> props = new Hashtable<String, String>();
		props.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.crawler.source");
		props.put(IDataProvider.PROP_DATAPROVIDER_ID, "org.paxle.parser.sink");
		
		context.registerService(IDataProvider.class.getName(), pipe, props);
		context.registerService(IDataConsumer.class.getName(), pipe, props);
	}

	public void stop(BundleContext context) throws Exception {
	}
}