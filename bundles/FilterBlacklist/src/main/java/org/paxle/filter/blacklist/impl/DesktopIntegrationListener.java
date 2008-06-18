package org.paxle.filter.blacklist.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.paxle.desktop.DIComponent;

public class DesktopIntegrationListener implements BundleListener {
	private ServiceRegistration dialogueReg = null;
	private final BundleContext bc;
	
	public DesktopIntegrationListener(BundleContext bc) {
		this.bc = bc;
	}
	
	public void bundleChanged(BundleEvent event) {
		if (event.getBundle().getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.desktop")) {
			if (event.getType() == BundleEvent.STARTED) {
				this.dialogueReg = this.bc.registerService(DIComponent.class.getName(), new BlacklistDialogue(), null);
			} else if (event.getType() == BundleEvent.STOPPED && this.dialogueReg != null) {
				this.dialogueReg.unregister();
				this.dialogueReg = null;
			}
		}
	}
}
