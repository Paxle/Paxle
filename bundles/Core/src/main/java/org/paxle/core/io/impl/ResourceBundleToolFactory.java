package org.paxle.core.io.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

public class ResourceBundleToolFactory implements ServiceFactory {

	public Object getService(Bundle bundle, ServiceRegistration registration) {
		return new ResourceBundleTool(bundle);
	}

	public void ungetService(Bundle bundle, ServiceRegistration registration, Object service) {
		// nothing todo here
	}
}
