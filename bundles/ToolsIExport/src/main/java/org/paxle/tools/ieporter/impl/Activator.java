
package org.paxle.tools.ieporter.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.tools.ieporter.cm.IConfigurationIEPorter;
import org.paxle.tools.ieporter.cm.impl.ConfigurationIEPorter;

public class Activator implements BundleActivator {

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
        bc.registerService(IConfigurationIEPorter.class.getName(), new ConfigurationIEPorter(bc), null);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {

	}
}
