package org.paxle.se.index.solr.impl;

import java.net.URL;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.data.IDataConsumer;
import org.paxle.se.index.IFieldManager;

public class Activator implements BundleActivator {

	private SolrWriter indexWriterThread = null;
	
	/**
	 * @see org.osgi.framework.BundleActivator#start(BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		IFieldManager fieldManager = (IFieldManager)context.getService(context.getServiceReference(IFieldManager.class.getName()));
		
		this.indexWriterThread = new SolrWriter(fieldManager, new URL("http://localhost:8983/solr"));
		
		// publish data source
		final Hashtable<String,String> sinkp = new Hashtable<String,String>();
		sinkp.put(IDataConsumer.PROP_DATACONSUMER_ID, "org.paxle.indexer.source");
		context.registerService(IDataConsumer.class.getName(), this.indexWriterThread, sinkp);

		indexWriterThread.start();
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}
}