/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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
import java.util.HashSet;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

@Component(metatype=false, name=PaxleCoreMonitoring.PID)
@Service(Monitorable.class)
@Property(name="Monitorable-Localization", value=PaxleCoreMonitoring.RB_BASENAME)
public class PaxleCoreMonitoring implements Monitorable {
	/**
	 * The {@link Constants#SERVICE_PID} of this {@link Monitorable}
	 */
	static final String PID = "org.paxle.core";
	
	/**
	 * {@link ResourceBundle} basename
	 */
	static final String RB_BASENAME = "OSGI-INF/l10n/PaxleCoreMonitoring";		
	
	/**
	 * Pattern to format dates
	 */
	private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
	
	/**
	 * The <code>Paxle-Release</code> header of the core-bundle
	 */
	private static final String VARNAME_RELEASE_VERSION = "release.version";
	
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
			add(VARNAME_RELEASE_VERSION);
			add(VARNAME_CORE_IMPL_VERSION);
			add(VARNAME_CORE_BUILD_VERSION);
			add(VARNAME_CORE_BUILD_TIME);
			add(VARNAME_CORE_INSTALL_TIME);
	}};	
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle(RB_BASENAME);
	
	/**
	 * An OSGi BundleContext required to get OSGi-Framework properties
	 */
	private BundleContext context;	
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Activate
	protected void activate(BundleContext context) {
		this.context = context;
	}

	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		return this.rb.getString(name);
	}

	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
				
		final Bundle core = this.context.getBundle();
		
		String value= null;
		if (name.equalsIgnoreCase(VARNAME_RELEASE_VERSION)) {
			value = (String) core.getHeaders().get("Paxle-Release");
		} else if (name.equalsIgnoreCase(VARNAME_CORE_IMPL_VERSION)) {
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
