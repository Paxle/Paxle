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

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;
import org.paxle.tools.logging.ILogData;
import org.paxle.tools.logging.ILogDataEntry;
import org.paxle.tools.logging.ILogReader;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="org.paxle.tools.logging.ILogReader"
 * @scr.property name="org.paxle.tools.logging.ILogReader.type" value="log4j"
 * @scr.property name="servlet.pid" value="org.paxle.tools.logging.impl.gui.Log4jView"
 */
public class LogReaderLog4j implements ILogReader {
	private static final long serialVersionUID = 1L;

	protected Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A internal buffer for logging-messages
	 */
	final Buffer fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(200));

	/**
	 * A in-memory log4j appender
	 */
	protected Log4jAppender appender;
	
	protected void activate(ComponentContext context) {
		// getting the Log4j root logger
		Logger rootLogger = Logger.getRootLogger();
		
		// configuring this class as memory appender
		this.appender = new Log4jAppender();
		rootLogger.addAppender(this.appender);		
	}
	
	protected void deactivate(ComponentContext context) {
		// getting the Log4j root logger
		Logger rootLogger = Logger.getRootLogger();
		
		// removing this class from the appenders
		rootLogger.removeAppender(this.appender);
		this.appender = null;
		
		// clear messages
		this.fifo.clear();
	}
	
	@SuppressWarnings("unchecked")
	public ILogData getLogData() {
		return new LogData(this.fifo);
	}	
	
	private class Log4jAppender extends AppenderSkeleton {
		@SuppressWarnings("unchecked")
		@Override
		protected void append(LoggingEvent event) {
			fifo.add(new Entry(event));
		}
	
		@Override
		public void close() {
			// cleanup buffer
			fifo.clear();
		}
	
		@Override
		public boolean requiresLayout() {
			return false;
		}
	}	
	
	private static class Entry implements ILogDataEntry {
		private final LoggingEvent log4jevent;

		public Entry(LoggingEvent log4jevent) {
			this.log4jevent = log4jevent;
		}

		public Bundle getBundle() {
			return null;
		}

		public Throwable getException() {
			ThrowableInformation ti = log4jevent.getThrowableInformation();
			return (ti == null)?null:ti.getThrowable();
		}

		public int getLevel() {
			Level level = this.log4jevent.getLevel();
			switch (level.toInt()) {
				case Level.FATAL_INT:
				case Level.ERROR_INT:
					return LogService.LOG_ERROR;
	
				case Level.WARN_INT:
					return LogService.LOG_WARNING;
					
				case Level.INFO_INT:
					return LogService.LOG_INFO;
					
				case Level.DEBUG_INT:
				case Level.TRACE_INT:
					return LogService.LOG_DEBUG;
				
				default:
					return LogService.LOG_DEBUG;
			}
		}

		public String getMessage() {
			return this.log4jevent.getMessage().toString();
		}

		public ServiceReference getServiceReference() {
			return null;
		}

		public long getTime() {
			return this.log4jevent.timeStamp;
		}

		public String getLoggerName() {
			return this.log4jevent.getLoggerName();
		}

	}
}
