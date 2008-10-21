/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.mimetype.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.mimetype.IMimeTypeDetector;

public class Activator implements BundleActivator {
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext bc) throws Exception {
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		// register the SubParser-Manager as service
		MimeTypeDetector detector = new MimeTypeDetector(null);
		bc.registerService(IMimeTypeDetector.class.getName(), detector , null);
		
		/* ==========================================================
		 * Register Service Listeners
		 * ========================================================== */		
		bc.addServiceListener(new DetectionHelperListener(detector,bc),DetectionHelperListener.FILTER);	
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */		
	public void stop(BundleContext context) throws Exception {
		// nothing todo here
	}
}