/*
 * Created on Sun Dec 09 15:57:16 GMT+01:00 2007
 */
package org.paxle.se.index.mg4j.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.data.IDataConsumer;
import org.paxle.se.index.IFieldManager;

public class Activator implements BundleActivator {
	public static Mg4jWriter indexWriterThread = null;
	
	
	public void start(BundleContext context) throws Exception {
		IFieldManager fieldManager = (IFieldManager)context.getService(context.getServiceReference(IFieldManager.class.getName()));
		
		indexWriterThread = new Mg4jWriter(fieldManager);
		
		// publish data source
		final Hashtable<String,String> sinkp = new Hashtable<String,String>();
		sinkp.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
		// context.registerService(IDataConsumer.class.getName(), indexWriterThread, sinkp);

		indexWriterThread.start();
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}
}