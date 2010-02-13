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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

@Component(metatype=false, name=NetworkMonitoring.PID)
@Service(Monitorable.class)
@Property(name="Monitorable-Localization", value=NetworkMonitoring.RB_BASENAME)
public class NetworkMonitoring implements Monitorable {
	/**
	 * The {@link Constants#SERVICE_PID} of this {@link Monitorable}
	 */
	static final String PID = "os.network";
	
	/**
	 * {@link ResourceBundle} basename
	 */
	static final String RB_BASENAME = "OSGI-INF/l10n/NetworkMonitoring";
	
	/**
	 * The hostname of the peer
	 */
	private static final String VAR_NAME_HOSTNAME = "hostname";
	
	/**
	 * The IP-address of the peer
	 */
	private static final String VAR_NAME_IP_ADDRESS = "ip-address";
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(){{
			add(VAR_NAME_HOSTNAME);
			add(VAR_NAME_IP_ADDRESS);
	}};	

	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle(RB_BASENAME);	
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
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
		
		String value= null;
		try {
			final InetAddress localhost = InetAddress.getLocalHost();
			if (name.equals(VAR_NAME_HOSTNAME)) {				
				value = localhost.getCanonicalHostName();
			} else if (name.equals(VAR_NAME_IP_ADDRESS)) {
				value = localhost.getHostAddress();
			}
		} catch (UnknownHostException e) {
			this.logger.error(e);
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
