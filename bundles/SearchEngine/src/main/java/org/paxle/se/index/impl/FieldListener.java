/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
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
