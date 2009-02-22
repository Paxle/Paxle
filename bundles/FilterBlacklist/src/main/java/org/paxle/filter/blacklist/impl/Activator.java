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
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.blacklist.impl.desktop.DesktopIntegrationListener;
import org.paxle.filter.blacklist.impl.gui.GuiListener;

public class Activator implements BundleActivator {
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		
		// creating blacklist data dir
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + "blacklist";		
		File list = new File(dataPath);
		list.mkdirs();
		new File(list, "default.list");
		
		/*
		 * Registering the filter
		 */
		Hashtable<String, Object> filterProps = new Hashtable<String, Object>();
		filterProps.put(Constants.SERVICE_PID, BlacklistFilter.class.getName());
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in;" + IFilter.PROP_FILTER_TARGET_POSITION + "=-1", "org.paxle.parser.out;" + IFilter.PROP_FILTER_TARGET_POSITION + "=66"});
		BlacklistFilter blacklistFilter = new BlacklistFilter(list);
		bc.registerService(IFilter.class.getName(), blacklistFilter, filterProps);
		
		/*
		 * Registering the bundle listeners
		 */
		bc.addBundleListener(new DesktopIntegrationListener(bc, blacklistFilter));
		bc.addBundleListener(new GuiListener(bc, blacklistFilter));
	}
	
	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		/* nothing todo here */
	}
}
