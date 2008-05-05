package org.paxle.dbus.impl;

import org.freedesktop.NetworkManager;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.paxle.core.IMWComponent;;

/**
 * @see http://people.redhat.com/dcbw/NetworkManager/NetworkManager%20DBUS%20API.txt
 */
public class CrawlerListener implements ServiceListener {

	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static String FILTER = String.format("(&(%s=%s)(component.ID=org.paxle.crawler))",
											    Constants.OBJECTCLASS, IMWComponent.class.getName());		
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;
	
	/**
	 * DBus Network-Manager monitor
	 */
	private NetworkManagerMonitor netMon = null;
	
	public CrawlerListener(BundleContext context, NetworkManagerMonitor netMon) throws InvalidSyntaxException {
		this.context = context;
		this.netMon = netMon;
		
		ServiceReference[] services = context.getServiceReferences(null,FILTER);
		if (services != null) for (ServiceReference service : services) serviceChanged(service, ServiceEvent.REGISTERED);	
	}
	
	/**
	 * @see DBusSigHandler#handle(DBusSignal)
	 */
	public void handle(DBusSignal signal) {
		if (signal instanceof NetworkManager.DeviceNoLongerActive) {
			System.out.println("Device was deactivated: " + signal.toString());
		} else if (signal instanceof NetworkManager.DeviceNowActive) {
			System.out.println("Device was activated: " + signal.toString());
		} else if (signal instanceof NetworkManager.DevicesChanged) {
			System.out.println("Device was changed: " + signal.toString());
		}
	}

	public void serviceChanged(ServiceEvent event) {
		// getting the service reference
		ServiceReference reference = event.getServiceReference();
		this.serviceChanged(reference, event.getType());		
	}
	
	private void serviceChanged(ServiceReference reference, int eventType) {
		if (reference == null) return;	
		
		if (eventType == ServiceEvent.REGISTERED) {			
			// a reference to the service
			IMWComponent crawler = (IMWComponent) this.context.getService(reference);			
			
			// new service was installed
			this.netMon.registerSignalListener(crawler);
			
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			// service was uninstalled
			this.netMon.unregisterSignalListener();
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}	
	}
}
