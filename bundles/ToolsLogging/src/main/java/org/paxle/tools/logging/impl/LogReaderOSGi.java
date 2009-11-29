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

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.paxle.tools.logging.ILogData;
import org.paxle.tools.logging.ILogDataEntry;
import org.paxle.tools.logging.ILogReader;

@Component(immediate=true, metatype=true)
@Service(ILogReader.class)
@Property(name=ILogReader.TYPE, value="osgi", propertyPrivate=true)
public class LogReaderOSGi implements LogListener, ILogReader {	
	
	@Property(intValue = 200)
	public static final String BUFFER_SIZE = "bufferSize";	
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY)
	private LogReaderService osgiLogReader;	
	
	/**
	 * A internal buffer for logging-messages
	 */
	Buffer fifo = null;

	protected void activate(Map<String, Object> props) {
		// configuring the buffer
		Integer bufferSize = Integer.valueOf(200);
		if (props.containsKey(BUFFER_SIZE)) {
			bufferSize = (Integer) props.get(BUFFER_SIZE);
		}
		this.fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(bufferSize));			
		
		// adding this class as log-listener
		this.osgiLogReader.addLogListener(this);
	}	
	
	protected void deactivate() {
		// removing this class from the log-listeners
		this.osgiLogReader.removeLogListener(this);
		
		// clear messages
		this.fifo.clear();		
	}		
	
	@SuppressWarnings("unchecked")
	public void logged(LogEntry logEntry) {
		this.fifo.add(new Entry(logEntry));
	}

	@SuppressWarnings("unchecked")
	public ILogData getLogData() {
		return new LogData(this.fifo);
	}

	private static class Entry implements ILogDataEntry {
		private final LogEntry logEntry;
		
		public Entry(LogEntry logEntry) {
			this.logEntry = logEntry;
		}
		
		public String getLoggerName() {
			final String bundleName = this.getBundle().getSymbolicName();
			final ServiceReference ref = this.getServiceReference();
			final Long servicePID = ref == null ? null : (Long)ref.getProperty(Constants.SERVICE_ID);			
			return String.format("%s/%d", bundleName, servicePID);
		}

		public Bundle getBundle() {
			return this.logEntry.getBundle();
		}

		public Throwable getException() {
			return this.logEntry.getException();
		}

		public int getLevel() {
			return this.logEntry.getLevel();
		}

		public String getMessage() {
			return this.logEntry.getMessage();
		}

		public ServiceReference getServiceReference() {
			return this.logEntry.getServiceReference();
		}

		public long getTime() {
			return this.logEntry.getTime();
		}
		
	}
}
