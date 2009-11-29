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
package org.paxle.tools.logging.impl;

import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;
import org.paxle.tools.logging.ILogDataEntry;
import org.paxle.tools.logging.ILogReader;

@Component(immediate=true, metatype=true)
@Service(ILogReader.class)
@Properties({
	@Property(name=ILogReader.TYPE, value="java.util.logging", propertyPrivate = true)
})
public class LogReaderJul extends ALogReader implements ILogReader {
	private static final long serialVersionUID = 1L;

	/**
	 * A in-memory java.logging handler
	 */
	protected JulHandler handler;
	
	protected void activate(Map<String, Object> props) {
		super.activate(props);
		
		// getting the root logger
		Logger rootLogger = Logger.getLogger("");
		
		// configuring this class as memory appender
		this.handler = new JulHandler();
		rootLogger.addHandler(this.handler);		
	}
	
	protected void deactivate() {
		// getting the Log4j root logger
		Logger rootLogger = Logger.getLogger("");
		
		// removing this class from the appenders
		rootLogger.removeHandler(this.handler);
		this.handler = null;
		
		// cleanup
		super.deactivate();
	}
	
	private class JulHandler extends Handler {
		@Override
		public void close() throws SecurityException {}

		@Override
		public void flush() {}

		@SuppressWarnings("unchecked")
		@Override
		public void publish(LogRecord record) {
			fifo.add(new Entry(record));
		}

	}	
	
	private static class Entry implements ILogDataEntry {
		private final LogRecord logRecord;

		public Entry(LogRecord logRecord) {
			this.logRecord = logRecord;
		}

		public Bundle getBundle() {
			return null;
		}

		public Throwable getException() {
			return this.logRecord.getThrown();
		}

		public int getLevel() {
			final Level level = this.logRecord.getLevel();
			if (level.equals(Level.SEVERE)) {
				return LogService.LOG_ERROR;
			} else if (level.equals(Level.WARNING)) {
				return LogService.LOG_WARNING;
			} else if (level.equals(Level.INFO)) {
				return LogService.LOG_INFO;
			} else if (
				level.equals(Level.FINE) || 
				level.equals(Level.FINER) || 
				level.equals(Level.FINEST)
			) { 
				return LogService.LOG_DEBUG;
			} else {
				return LogService.LOG_DEBUG;
			}
		}

		public String getMessage() {
			return this.logRecord.getMessage();
		}

		public ServiceReference getServiceReference() {
			return null;
		}

		public long getTime() {
			return this.logRecord.getMillis();
		}

		public String getLoggerName() {
			return this.logRecord.getLoggerName();
		}

	}
}
