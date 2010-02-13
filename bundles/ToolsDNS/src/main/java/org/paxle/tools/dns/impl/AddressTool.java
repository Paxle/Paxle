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

package org.paxle.tools.dns.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.osgi.framework.Constants;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.tools.dns.IAddressTool;
import org.xbill.DNS.Address;
import org.xbill.DNS.ResolverConfig;

/**
 * Just a wrapper for {@link org.xbill.DNS.Address}
 */
@Component(name=AddressTool.PID, metatype=false)
@Services({
	@Service(IAddressTool.class),
	@Service(Monitorable.class)
})
@Property(name="Monitorable-Localization", value=AddressTool.RB_BASENAME)
public class AddressTool implements IAddressTool, Monitorable {
	/**
	 * The {@link Constants#SERVICE_PID} of this {@link Monitorable}
	 */
	static final String PID = "org.paxle.dns";	
	
	/**
	 * {@link ResourceBundle} basename
	 */
	static final String RB_BASENAME = "OSGI-INF/l10n/AddressTool";		
	
	/**
	 * The primary dns-server that is used
	 */
	private static final String VARNAME_DNS_SERVER = "dns.server";	
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(){{
			add(VARNAME_DNS_SERVER);
	}};	
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle(RB_BASENAME);	
	
	/**
	 * The DNS server configuration
	 */
	private ResolverConfig config;
	
	protected void activate(Map<String, Object> props) {
		this.config = ResolverConfig.getCurrentConfig();
	}
	
	public InetAddress getByName(String hostName) throws UnknownHostException {
		return Address.getByName(hostName);
	}

	public String getServer() {
		return this.config.server();
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
		
		String value= null;
		if (name.equals(VARNAME_DNS_SERVER)) {
			value = this.getServer();
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
		boolean done = false;
		
		if (name.equals(VARNAME_DNS_SERVER)) {
			ResolverConfig.refresh();
			this.config = ResolverConfig.getCurrentConfig();
			done = true;
		}
		
		return done;
	}
}
