package org.paxle.p2p.shell.impl;

import java.util.Arrays;
import java.util.HashSet;

import net.jxta.impl.shell.bin.Shell.Shell;
import net.jxta.peergroup.PeerGroup;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

public class GroupListener implements ServiceListener {
	
	private Shell shell = null;
	
	/**
	 * A LDAP styled expression used for the service-listener
	 */
	public static final String FILTER = String.format("(objectClass=%s)",PeerGroup.class.getName());
	
	/**
	 * The {@link BundleContext osgi-bundle-context} of this bundle
	 */	
	private BundleContext context = null;	

	public GroupListener(BundleContext context) {
		this.context = context;
		ServiceReference reference = context.getServiceReference(PeerGroup.class.getName());
		this.serviceChanged(reference, ServiceEvent.REGISTERED);
	}
	
	/**
	 * @see ServiceListener#serviceChanged(ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		// getting the service reference
		ServiceReference reference = event.getServiceReference();
		this.serviceChanged(reference, event.getType());
	}
	
	private void serviceChanged(ServiceReference reference, int eventType) {
		if (reference == null) return;		
		if (eventType == ServiceEvent.REGISTERED) {			
			// get the detector service
			PeerGroup paxleGroup = (PeerGroup) this.context.getService(reference);
			
			//System.setProperty("SHELLNOWINDOW", "true");
			
			shell = new Shell(true);
			shell.init(paxleGroup,null,null);
			shell.startApp(null);			
		} else if (eventType == ServiceEvent.UNREGISTERING) {
			shell.stopApp();
		} else if (eventType == ServiceEvent.MODIFIED) {
			// service properties have changed
		}	
	}
}
