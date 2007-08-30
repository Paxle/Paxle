
package org.paxle.crypt.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.paxle.core.crypt.ICrypt;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		for (final Impls impl : Impls.values()) {
			final Hashtable<String,String> props = new Hashtable<String,String>();
			props.put(ICrypt.CRYPT_NAME_PROP, impl.name);
			bc.registerService(ICrypt.class.getName(), impl, props);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc = null;
	}
}
