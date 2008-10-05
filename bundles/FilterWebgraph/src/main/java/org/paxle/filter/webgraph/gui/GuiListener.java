package org.paxle.filter.webgraph.gui;

import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.paxle.filter.webgraph.impl.GraphFilter;

public class GuiListener implements BundleListener {
	private ServiceRegistration serviceReg = null;
	private final BundleContext bc;
	private GraphFilter filter;
	
	public GuiListener(BundleContext bc, GraphFilter filter) {
		this.bc = bc;
		this.filter=filter;
	}
	
	public void bundleChanged(BundleEvent event) {
		if (event.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.gui")) {
			if (event.getType() == BundleEvent.STARTED) {
				/*
				 * Registering the servlet
				 */
				registerServlet();
			} else if (event.getType() == BundleEvent.STOPPED && this.serviceReg != null) {
				this.serviceReg.unregister();
				this.serviceReg = null;
			}
		}
	}

	public void registerServlet() {
		SourceServlet servlet=new SourceServlet(this.filter);
		//servlet.setBundleLocation(bc.getBundle().getEntry("/").toString());
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("path", "/domaingraphsource");
		this.serviceReg = bc.registerService("javax.servlet.Servlet", servlet, props);
	}
}

