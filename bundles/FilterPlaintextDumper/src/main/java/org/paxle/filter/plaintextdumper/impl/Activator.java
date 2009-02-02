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
package org.paxle.filter.plaintextdumper.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;

public class Activator implements BundleActivator {
	private static String PATH = "plaintext-dumper";	

	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		// create data-dir if required
		final File dir = new File(PATH);
		if (!dir.exists()) dir.mkdirs();
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		final Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {
				// apply filter to the parser-output-queue at MAX-Position and disable filter by default
				String.format("org.paxle.parser.out;%s=%d;%s=%b", 
						IFilter.PROP_FILTER_TARGET_POSITION, Integer.valueOf(Integer.MAX_VALUE), 
						IFilter.PROP_FILTER_TARGET_DISABLED, Boolean.TRUE)
		});
		
		bc.registerService(IFilter.class.getName(), new PlaintextDumperFilter(dir), filterProps);
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		// nothing todo here
	}
}