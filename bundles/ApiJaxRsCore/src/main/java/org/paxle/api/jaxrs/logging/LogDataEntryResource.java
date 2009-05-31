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
package org.paxle.api.jaxrs.logging;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import org.osgi.service.log.LogService;
import org.paxle.tools.logging.ILogDataEntry;

public class LogDataEntryResource {
	@SuppressWarnings("serial")
	static final HashMap<Integer, String> LOGLEVEL_NAMES = new HashMap<Integer, String>(){{
		put(Integer.valueOf(LogService.LOG_ERROR) , "ERROR");
		put(Integer.valueOf(LogService.LOG_WARNING), "WARN");
		put(Integer.valueOf(LogService.LOG_INFO), "INFO");
		put(Integer.valueOf(LogService.LOG_DEBUG), "DEBUG");
	}};	
	
	@SuppressWarnings("serial")
	static final HashMap<String,Integer> LOGLEVEL_VALUES = new HashMap<String,Integer>(){{
		put("ERROR",Integer.valueOf(LogService.LOG_ERROR));
		put("WARN",Integer.valueOf(LogService.LOG_WARNING));
		put("INFO",Integer.valueOf(LogService.LOG_INFO));
		put("DEBUG",Integer.valueOf(LogService.LOG_DEBUG));
	}};
	
	private final ILogDataEntry entry;
	
	public LogDataEntryResource(ILogDataEntry entry) {
		this.entry = entry;
	}
	
	public String getLogger() {
		return this.entry.getLoggerName();
	}
	
	public long getTimestamp() {
		return this.entry.getTime();
	}
	
	public String getLevel() {
		return LOGLEVEL_NAMES.get(this.entry.getLevel());
	}
	
	public String getMessage() {
		return this.entry.getMessage();
	}
	
	public String getThrowable() {
		if (this.entry.getException() == null) return null;
		
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintStream errorOut = new PrintStream(bout,false,"UTF-8");
			this.entry.getException().printStackTrace(errorOut);

			errorOut.flush();
			errorOut.close();
			return bout.toString("UTF-8");
		} catch (Exception ex) {
			// should not occur
			return null;
		}
	}
}
