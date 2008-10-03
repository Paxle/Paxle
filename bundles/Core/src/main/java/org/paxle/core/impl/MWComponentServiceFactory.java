package org.paxle.core.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.paxle.core.IMWComponentFactory;

/**
 * This {@link ServiceFactory} creates a new {@link MWComponentFactory} if 
 * a bundle requests the {@link IMWComponentFactory}-service.
 *  
 * Please note that each created {@link MWComponentFactory} is bundle specific. 
 */
public class MWComponentServiceFactory implements ServiceFactory {
	
	private final String[] locales;
	
	public MWComponentServiceFactory(final String[] locales) {
		this.locales = locales;
	}
	
	/**
	 * @see ServiceFactory#getService(Bundle, ServiceRegistration)
	 */
	public IMWComponentFactory getService(Bundle bundle, ServiceRegistration registration) {
		return new MWComponentFactory(bundle, locales);
	}

	/**
	 * @see ServiceFactory#ungetService(Bundle, ServiceRegistration, Object)
	 */
	public void ungetService(Bundle bundle, ServiceRegistration registration, Object arg2) {
		// nothing todo here at the moment
	}
}
