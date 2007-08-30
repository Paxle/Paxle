
package org.paxle.core.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.crypt.ICrypt;

public class CryptListener implements ServiceListener {
	
	public static final String FILTER = String.format("(&(%s=%s)(%s=*))",
			Constants.OBJECTCLASS, ICrypt.class.getName(), ICrypt.CRYPT_NAME_PROP);
	
	private final BundleContext context;
	private final CryptManager manager;
	
	public CryptListener(BundleContext context, CryptManager manager) {
		this.context = context;
		this.manager = manager;
	}
	
	public void serviceChanged(ServiceEvent event) {
		final ServiceReference ref = event.getServiceReference();
		final String name = (String)ref.getProperty(ICrypt.CRYPT_NAME_PROP);
		
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				final ICrypt crypt = (ICrypt)this.context.getService(ref);
				this.manager.addCrypt(name, crypt);
				break;
			case ServiceEvent.UNREGISTERING:
				this.manager.removeCrypt(name);
				break;
			case ServiceEvent.MODIFIED:
				break;
		}
	}
}
