package org.paxle.filter.blacklist.impl.gui;

import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.paxle.filter.blacklist.impl.BlacklistFilter;

public class GuiListener implements BundleListener {
	private ServiceRegistration serviceReg = null;
	private final BundleContext bc;
	private final BlacklistFilter blacklistFilter;
	
	public GuiListener(BundleContext bc, BlacklistFilter blacklistFilter) {
		this.bc = bc;
		this.blacklistFilter = blacklistFilter;
	}
	
	public void bundleChanged(BundleEvent event) {
		if (event.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.gui")) {
			if (event.getType() == BundleEvent.STARTED) {
				/*
				 * Registering the servlet
				 */
				BlacklistServlet servlet = new BlacklistServlet(blacklistFilter);
				servlet.init(bc.getBundle().getEntry("/").toString(),"/blacklist");
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("path", "/blacklist");
				props.put("menu", "Blacklist");
				this.serviceReg = bc.registerService("javax.servlet.Servlet", servlet, props);
			} else if (event.getType() == BundleEvent.STOPPED && this.serviceReg != null) {
				this.serviceReg.unregister();
				this.serviceReg = null;
			}
		}
	}
}
