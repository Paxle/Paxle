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
package org.paxle.core.crypt.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.crypt.ICrypt;
import org.paxle.core.crypt.ICryptManager;

@Component(immediate=true, metatype=false)
@Service(ICryptManager.class)
@Reference(
	name=CryptManager.REFERENCE_CRYPT_PROVIDERS,
	referenceInterface=ICrypt.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addCryptProvider",
	unbind="removeCryptManagerProvider",
	target="(crypt.name=*)"
)
public class CryptManager implements ICryptManager {
	static final String REFERENCE_CRYPT_PROVIDERS = "cryptProviders";
	
	private final Hashtable<String,ServiceReference> crypts = new Hashtable<String,ServiceReference>();
	
	/**
	 * The context of this component
	 */
	protected ComponentContext ctx;		
	
	/**
	 * for logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());

	protected void activate(ComponentContext context) {
		this.ctx = context;
	}
	
	public ICrypt getCrypt(String name) {
		if (name == null) return null;
		
		final ServiceReference ref = this.crypts.get(name);
		if (ref == null) return null;
		return (ICrypt) this.ctx.locateService(REFERENCE_CRYPT_PROVIDERS, ref);
	}
	
	protected void addCryptProvider(ServiceReference providerRef) {
		final String name = (String) providerRef.getProperty(ICrypt.CRYPT_NAME_PROP);
		if (name == null) return;
		
		this.crypts.put(name, providerRef);
		this.logger.info(String.format(
				"Crypt provider with PID '%s' from bundle '%s' registered.",
				providerRef.getProperty(Constants.SERVICE_PID),
				providerRef.getBundle().getSymbolicName()
		));		
	}
	
	protected void removeCryptManagerProvider(ServiceReference providerRef) {
		final String name = (String) providerRef.getProperty(ICrypt.CRYPT_NAME_PROP);
		if (name == null) return;		
		
		this.crypts.remove(name);
		this.logger.info(String.format(
				"Crypt provider with PID '%s' from bundle '%s' unregistered.",
				providerRef.getProperty(Constants.SERVICE_PID),
				providerRef.getBundle().getSymbolicName()
		));		
	}
	
	public Collection<ICrypt> getCrypts() {
		final ArrayList<ICrypt> providers = new ArrayList<ICrypt>();
		
		for (String name : this.crypts.keySet()) {
			providers.add(this.getCrypt(name));
		}
		
		return providers;
	}
	
	public Collection<String> getNames() {
		return new HashSet<String>(this.crypts.keySet());
	}
}
