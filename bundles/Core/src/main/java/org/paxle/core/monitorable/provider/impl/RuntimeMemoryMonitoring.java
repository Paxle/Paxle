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

import java.util.HashSet;
import java.util.ResourceBundle;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;

@Component(metatype=false, name=RuntimeMemoryMonitoring.PID)
@Service(Monitorable.class)
@Property(name="Monitorable-Localization", value=RuntimeMemoryMonitoring.RB_BASENAME)
public class RuntimeMemoryMonitoring implements Monitorable {
	/**
	 * The {@link Constants#SERVICE_PID} of this {@link Monitorable}
	 */
	static final String PID = "java.lang.runtime";
	
	/**
	 * {@link ResourceBundle} basename
	 */
	static final String RB_BASENAME = "OSGI-INF/l10n/RuntimeMemoryMonitoring";
	
	private static final String MEMORY_FREE = "memory.free";
	private static final String MEMORY_MAX = "memory.max";
	private static final String MEMORY_TOTAL = "memory.total";
	private static final String MEMORY_USED = "memory.used";

	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	@SuppressWarnings("serial")
	private static final HashSet<String> VAR_NAMES = new HashSet<String>() {{
		add(MEMORY_FREE);
		add(MEMORY_MAX);
		add(MEMORY_TOTAL);
		add(MEMORY_USED);
	}};

	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle(RB_BASENAME);	
	
	/**
	 * @see Monitorable#getStatusVariableNames()
	 */
	public String[] getStatusVariableNames() {
		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
	}	
	
	/**
	 * @see Monitorable#getDescription(String)
	 */
	public String getDescription(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		return this.rb.getString(name);
	}

	/**
	 * @see Monitorable#getStatusVariable(String)
	 */
	public StatusVariable getStatusVariable(String name) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(name)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + name);
		}
		
		long mem = 0;
		final Runtime rt = Runtime.getRuntime();		

		if (name.equals(MEMORY_TOTAL)) mem = rt.totalMemory(); 
		else if (name.equals(MEMORY_MAX)) mem = rt.maxMemory();
		else if (name.equals(MEMORY_FREE)) mem = rt.maxMemory() - rt.totalMemory() + rt.freeMemory();
		else if (name.equals(MEMORY_USED)) mem = rt.totalMemory() - rt.freeMemory();
		
		return new StatusVariable(
				name,
				StatusVariable.CM_GAUGE,
				(int)mem
		);
	}

	/**
	 * @see Monitorable#notifiesOnChange(String)
	 */
	public boolean notifiesOnChange(String name) throws IllegalArgumentException {
		return false;
	}

	/**
	 * @see Monitorable#resetStatusVariable(String)
	 */
	public boolean resetStatusVariable(String name) throws IllegalArgumentException {
		return false;
	}

}
