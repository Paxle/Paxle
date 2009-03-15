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
package org.paxle.core.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.paxle.core.IMWComponentFactory;

/**
 * This {@link ServiceFactory} creates a new {@link MWComponentFactory} if 
 * a bundle requests the {@link IMWComponentFactory}-service.
 *  
 * Please note that each created {@link MWComponentFactory} is bundle specific. 
 */
public class MWComponentServiceFactory implements ServiceFactory {
	
	private final String[] locales;
	
	public MWComponentServiceFactory(final String[] locales) {
		this.locales = locales==null?null:locales.clone();
	}
	
	/**
	 * @see ServiceFactory#getService(Bundle, ServiceRegistration)
	 */
	public IMWComponentFactory getService(Bundle bundle, ServiceRegistration registration) {
		return new MWComponentFactory(bundle, locales);
	}

	/**
	 * @see ServiceFactory#ungetService(Bundle, ServiceRegistration, Object)
	 */
	public void ungetService(Bundle bundle, ServiceRegistration registration, Object arg2) {
		// nothing todo here at the moment
	}
}
