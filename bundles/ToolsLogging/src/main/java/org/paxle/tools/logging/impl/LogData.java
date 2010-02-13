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

package org.paxle.tools.logging.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.paxle.tools.logging.ILogData;
import org.paxle.tools.logging.ILogDataEntry;

public class LogData implements ILogData {
	private ArrayList<ILogDataEntry> logData = new ArrayList<ILogDataEntry>();
	private HashMap<Integer, Integer> logStats = new HashMap<Integer, Integer>();
	
	public LogData(Collection<ILogDataEntry> fifo) {
		this(fifo,0);
	}
	
	public LogData(Collection<ILogDataEntry> fifo, long timestamp) {
		for (ILogDataEntry entry : fifo) {
			if (entry.getTime() <= timestamp) continue;
			
			Integer logLevel = Integer.valueOf(entry.getLevel());
			
			// count message-types
			logStats.put(
					logLevel,
					Integer.valueOf(logStats.containsKey(logLevel)?logStats.get(logLevel).intValue() + 1 : 1)
			);
			
			// remember message
			this.logData.add(entry);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.tools.logging.impl.gui.ILogData#getLog()
	 */
	public List<ILogDataEntry> getLog() {
		return this.logData;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.tools.logging.impl.gui.ILogData#getStatistics()
	 */
	public Map<Integer,Integer> getStatistics() {
		return this.logStats;
	}
}
