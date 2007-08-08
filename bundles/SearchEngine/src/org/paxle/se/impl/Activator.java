package org.paxle.se.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	private static ServiceListener mlistener = null;
	private static ServiceListener slistener = null;
	private static ServiceListener wlistener = null;
	private static ServiceListener tlistener = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc.removeServiceListener(mlistener);
		bc.removeServiceListener(slistener);
		bc.removeServiceListener(wlistener);
		bc.removeServiceListener(tlistener);
		mlistener = null;
		slistener = null;
		wlistener = null;
		tlistener = null;
		bc = null;
	}
}