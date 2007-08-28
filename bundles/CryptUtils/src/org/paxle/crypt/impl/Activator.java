
package org.paxle.crypt.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.crypt.md5.IMD5;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		bc.registerService(IMD5.class.getName(), new MD5(), null);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}
