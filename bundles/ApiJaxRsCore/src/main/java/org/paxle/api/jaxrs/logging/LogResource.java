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

package org.paxle.api.jaxrs.logging;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.paxle.tools.logging.ILogDataEntry;
import org.paxle.tools.logging.ILogReader;

public class LogResource {
	private final String type;
	private final ILogReader logReader;
	
	public LogResource(ILogReader reader, String type) {
		this.type = type;
		this.logReader = reader;
	}
	
	@GET
	public LogResource returnThis() {
		return this;
	}
	
	public String getType() {
		return this.type;
	}
	
	public List<LogDataEntryResource> getMessages() {
		return this.getMessages("DEBUG");
	}
	
	@GET
	@Path("{logLevel}")
	public List<LogDataEntryResource> getMessages(@PathParam("logLevel") String logLevel) {
		ArrayList<LogDataEntryResource> entries = new ArrayList<LogDataEntryResource>();
		
		Integer logLevelInt = LogDataEntryResource.LOGLEVEL_VALUES.get(logLevel);
		if (logLevelInt == null) logLevelInt = LogDataEntryResource.LOGLEVEL_VALUES.get("DEBUG");
		
		List<ILogDataEntry> entryList = this.logReader.getLogData().getLog();
		if (entryList != null && entryList.size() > 0) {
			for (ILogDataEntry entry : entryList) {
				if (entry.getLevel() <= logLevelInt) {
					entries.add(new LogDataEntryResource(entry));
				}
			}
		}
		
		return entries;
	}
}