package org.paxle.filter.blacklist.impl.desktop;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.paxle.desktop.DIComponent;
import org.paxle.filter.blacklist.impl.BlacklistFilter;

public class DesktopIntegrationListener implements BundleListener {
	private ServiceRegistration dialogueReg = null;
	private final BundleContext bc;
	private final BlacklistFilter blacklistFilter;
	
	public DesktopIntegrationListener(BundleContext bc, BlacklistFilter blacklistFilter) {
		this.bc = bc;
		this.blacklistFilter = blacklistFilter;
		for (final Bundle bundle : bc.getBundles())
			if (bundle.getState() == Bundle.ACTIVE)
				bundleChanged(bundle, BundleEvent.STARTED);
	}
	
	private void bundleChanged(final Bundle bundle, final int type) {
		if (bundle.getHeaders().get(Constants.BUNDLE_SYMBOLICNAME).equals("org.paxle.desktop")) {
			if (type == BundleEvent.STARTED) {
				this.dialogueReg = this.bc.registerService(DIComponent.class.getName(), new BlacklistDialogue(blacklistFilter), null);
			} else if (type == BundleEvent.STOPPED && this.dialogueReg != null) {
				this.dialogueReg.unregister();
				this.dialogueReg = null;
			}
		}
	}
	
	public void bundleChanged(BundleEvent event) {
		bundleChanged(event.getBundle(), event.getType());
	}
}
