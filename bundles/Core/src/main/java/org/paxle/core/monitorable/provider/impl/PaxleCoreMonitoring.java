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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

public class PaxleCoreMonitoring implements Monitorable {
	/**
	 * @see Constants#SERVICE_PID
	 */
	public static final String SERVICE_PID = "org.paxle.core";
	
	/**
	 * Pattern to format dates
	 */
	private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	/**
	 * The <code>Implementation-Version</code> header of the core-bundle
	 */
	private static final String VARNAME_CORE_IMPL_VERSION = "core.impl.version";
	
	/**
	 * The <code>Implementation-Build</code> header of the core-bundle
	 */
	private static final String VARNAME_CORE_BUILD_VERSION = "core.build.version";
	
	/**
	 * The <code>Bnd-LastModified</code> header of the core-bundle
	 */
	private static final String VARNAME_CORE_BUILD_TIME = "core.build.time";
	
	/**
	 * The install time of the core-bundle
	 */
	private static final String VARNAME_CORE_INSTALL_TIME = "core.install.time";
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(){{
			add(VARNAME_CORE_IMPL_VERSION);
			add(VARNAME_CORE_BUILD_VERSION);
			add(VARNAME_CORE_BUILD_TIME);
			add(VARNAME_CORE_INSTALL_TIME);
	}};	
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashMap<String, String> VAR_DESCRIPTIONS = new HashMap<String, String>(){{
		put(VARNAME_CORE_IMPL_VERSION,"The Implementation-Version header of the core-bundle.");
		put(VARNAME_CORE_BUILD_VERSION,"The Implementation-Build header of the core-bundle.");
		put(VARNAME_CORE_BUILD_TIME,"The Bnd-LastModified header of the core-bundle.");
		put(VARNAME_CORE_INSTALL_TIME,"The install time of the core-bundle.");
	}};
	
	/**
	 * An OSGi BundleContext required to get OSGi-Framework properties
	 */
	private final BundleContext bc;	
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	public PaxleCoreMonitoring(BundleContext bc) {
		this.bc = bc;		
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
				
		Bundle core = this.bc.getBundle();		
		String value= null;
		if (name.equalsIgnoreCase(VARNAME_CORE_IMPL_VERSION)) {
			value = (String) core.getHeaders().get("Implementation-Version");
		} else if (name.equalsIgnoreCase(VARNAME_CORE_BUILD_VERSION)) {
			value = (String) core.getHeaders().get("Implementation-Build");
		} else if (name.equalsIgnoreCase(VARNAME_CORE_BUILD_TIME)) {
			try {
				value = (String) core.getHeaders().get("Bnd-LastModified");
		        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
		        value = sdf.format(new Date(Long.parseLong(value)));
			} catch (NumberFormatException e) {
				this.logger.error(e);
			}
		} else if (name.equalsIgnoreCase(VARNAME_CORE_INSTALL_TIME)) {
			try {
		        SimpleDateFormat sdf = new SimpleDateFormat(TIME_FORMAT);
		        value = sdf.format(new Date(Long.valueOf(core.getLastModified())));
			} catch (NumberFormatException e) {
				this.logger.error(e);
			}
		}
		
		return new StatusVariable(name,StatusVariable.CM_SI,value==null?"":value);
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
