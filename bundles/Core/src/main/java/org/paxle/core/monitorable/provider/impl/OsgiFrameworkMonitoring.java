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
package org.paxle.core.monitorable.provider.impl;

import java.util.HashMap;
import java.util.HashSet;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

/**
 * @scr.component name="org.osgi.framework"
 * @scr.service interface="org.osgi.service.monitor.Monitorable"
 */
public class OsgiFrameworkMonitoring implements Monitorable {
	private static final String FRAMEWORK_PROP_PREFIX = "org.osgi.framework.";
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(){{
		add(Constants.FRAMEWORK_VERSION.substring(FRAMEWORK_PROP_PREFIX.length()));
		add(Constants.FRAMEWORK_VENDOR.substring(FRAMEWORK_PROP_PREFIX.length()));
		add(Constants.FRAMEWORK_LANGUAGE.substring(FRAMEWORK_PROP_PREFIX.length()));
		add(Constants.FRAMEWORK_OS_NAME.substring(FRAMEWORK_PROP_PREFIX.length()));
		add(Constants.FRAMEWORK_OS_VERSION.substring(FRAMEWORK_PROP_PREFIX.length()));
		add(Constants.FRAMEWORK_PROCESSOR.substring(FRAMEWORK_PROP_PREFIX.length()));
			// add(Constants.FRAMEWORK_EXECUTIONENVIRONMENT.substring(FRAMEWORK_PROP_PREFIX.length()))
	}};	
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashMap<String, String> VAR_DESCRIPTIONS = new HashMap<String, String>(){{
		put(Constants.FRAMEWORK_VERSION.substring(FRAMEWORK_PROP_PREFIX.length()), "The OSGi framework version.");
		put(Constants.FRAMEWORK_VENDOR.substring(FRAMEWORK_PROP_PREFIX.length()), "The OSGi framework vendor.");
		put(Constants.FRAMEWORK_LANGUAGE.substring(FRAMEWORK_PROP_PREFIX.length()), "The OSGi framework implementation language (see ISO 639 for possible values).");
		put(Constants.FRAMEWORK_OS_NAME.substring(FRAMEWORK_PROP_PREFIX.length()), "The OSGi framework host-computer's operating system.");
		put(Constants.FRAMEWORK_OS_VERSION.substring(FRAMEWORK_PROP_PREFIX.length()), "The OSGi framework host-computer's operating system version number.");
		put(Constants.FRAMEWORK_PROCESSOR.substring(FRAMEWORK_PROP_PREFIX.length()), "The OSGi framework host-computer's processor name.");
		// put(Constants.FRAMEWORK_EXECUTIONENVIRONMENT.substring(FRAMEWORK_PROP_PREFIX.length()), "The execution environments provided by the OSGi framework");
	}};
	
	/**
	 * An OSGi BundleContext required to get OSGi-Framework properties
	 */
	private BundleContext bc;
	
	protected void activate(ComponentContext context) {
		this.bc = context.getBundleContext();
	}
	
	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		return VAR_DESCRIPTIONS.get(name);
	}

	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		String propValue = this.bc.getProperty(FRAMEWORK_PROP_PREFIX + name);
		return new StatusVariable(name,StatusVariable.CM_SI,propValue==null?"":propValue);
	}

	public String[] getStatusVariableNames() {
		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
	}

	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
		return false;
	}

	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
		return false;
	}

}
