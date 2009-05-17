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

import java.util.Iterator;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

public class OSGiLogReader implements LogListener, ILogReader {
	final Buffer fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(200));

	@SuppressWarnings("unchecked")
	public void logged(LogEntry logEntry) {
		this.fifo.add(new Entry(logEntry));
	}

	@SuppressWarnings("unchecked")
	public Iterator<LogEntry> getLog() {
		return fifo.iterator();
	}

	@SuppressWarnings("unchecked")
	public LogData getLogData() {
		return new LogData(this.fifo);
	}

	private static class Entry implements LogDataEntry {
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
