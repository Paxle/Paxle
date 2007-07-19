package org.paxle.core.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.paxle.core.IMWComponentManager;

/**
 * This {@link ServiceFactory} creates a new {@link MWComponentManager} if 
 * a bundle requests the {@link IMWComponentManager}-service.
 *  
 * Please note that each created {@link MWComponentManager} is bundle specific. 
 */
public class MWComponentFactory implements ServiceFactory {

	/**
	 * @see ServiceFactory#getService(Bundle, ServiceRegistration)
	 */
	public Object getService(Bundle bundle, ServiceRegistration registration) {
		return new MWComponentManager(bundle);
	}

	/**
	 * @see ServiceFactory#ungetService(Bundle, ServiceRegistration, Object)
	 */
	public void ungetService(Bundle arg0, ServiceRegistration arg1, Object arg2) {
		// nothing todo here at the moment
	}
}
