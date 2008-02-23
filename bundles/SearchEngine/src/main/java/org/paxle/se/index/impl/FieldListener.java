
package org.paxle.se.index.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import org.paxle.core.doc.Field;

public class FieldListener implements ServiceListener {
	
	public static final String FILTER = String.format("(%s=%s)", Constants.OBJECTCLASS, Field.class.getName());
	
	private final Log logger = LogFactory.getLog(FieldListener.class);
	private final BundleContext context;
	private final FieldManager manager;
	
	public FieldListener(BundleContext context, FieldManager manager) {
		this.context = context;
		this.manager = manager;
		
		try {
			final ServiceReference[] refs = context.getServiceReferences(Field.class.getName(), null);
			if (refs != null) {
				for (final ServiceReference ref : refs)
					registered(ref);
			}
		} catch (InvalidSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	private void registered(ServiceReference ref) {
		final Field<?> field = (Field<?>)this.context.getService(ref);
		this.logger.debug("registered new field: " + field);
		this.manager.add(field);
	}
	
	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()) {
			case ServiceEvent.REGISTERED:
				registered(event.getServiceReference());
				break;
			case ServiceEvent.UNREGISTERING:
				/* ignore as Fields don't disappear from the index on unregistering a Field-provider */
				break;
			case ServiceEvent.MODIFIED:
				break;
		}
	}
}
