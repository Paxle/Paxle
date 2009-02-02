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
package org.paxle.gui.impl.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.service.log.LogEntry;

public class LogData {
	private ArrayList<LogEntry> logData = new ArrayList<LogEntry>();
	private HashMap<Integer, Integer> logStats = new HashMap<Integer, Integer>();
	
	public LogData(Collection<LogEntry> fifo) {
		this(fifo,0);
	}
	
	public LogData(Collection<LogEntry> fifo, long timestamp) {
		for (LogEntry entry : fifo) {
			if (entry.getTime() <= timestamp) continue;
			
			Integer logLevel = new Integer(entry.getLevel());
			
			// count message-types
			logStats.put(
					logLevel,
					new Integer(logStats.containsKey(logLevel)?logStats.get(logLevel).intValue() + 1 : 1)
			);
			
			// remember message
			this.logData.add(entry);
		}
	}
	
	/**
	 * @return a list of buffered {@link LogEntry log-messages}
	 */
	public List<LogEntry> getLog() {
		return this.logData;
	}
	
	/**
	 * @return a map containing the {@link LogEntry#getLevel() log-level} as key and 
	 * the number of {@link LogEntry messages} with this level as value.
	 */
	public Map<Integer,Integer> getStatistics() {
		return this.logStats;
	}
}
