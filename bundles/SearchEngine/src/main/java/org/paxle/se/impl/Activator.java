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
package org.paxle.se.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import org.paxle.core.prefs.IPropertiesStore;
import org.paxle.core.prefs.Properties;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.impl.FieldListener;
import org.paxle.se.index.impl.FieldManager;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.impl.SearchProviderListener;
import org.paxle.se.search.impl.SearchProviderManager;

public class Activator implements BundleActivator {
	
	public static BundleContext bc = null;
	
	public static FieldListener fieldListener = null;
	public static FieldManager fieldManager = null;
	
	public static SearchProviderManager searchProviderManager = null;
	public static SearchProviderListener searchProviderListener = null;
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		bc = context;
		
		/*
		 * Load the properties of this bundle
		 */
		Properties props = null;
		ServiceReference ref = bc.getServiceReference(IPropertiesStore.class.getName());
		if (ref != null) props = ((IPropertiesStore) bc.getService(ref)).getProperties(bc);				
		
		fieldManager = new FieldManager();
		bc.registerService(IFieldManager.class.getName(), fieldManager, null);
		fieldListener = new FieldListener(bc, fieldManager);
		bc.addServiceListener(fieldListener, FieldListener.FILTER);
		
		searchProviderManager = new SearchProviderManager(props);
		bc.registerService(ISearchProviderManager.class.getName(), searchProviderManager, null);
		searchProviderListener = new SearchProviderListener(searchProviderManager, bc);
		bc.addServiceListener(searchProviderListener, SearchProviderListener.FILTER);
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		bc.removeServiceListener(searchProviderListener);
		searchProviderManager.shutdown();
		searchProviderListener = null;
		searchProviderManager = null;
		bc = null;
	}
}
