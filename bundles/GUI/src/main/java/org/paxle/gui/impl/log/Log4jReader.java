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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;
import org.paxle.gui.ALayoutServlet;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="org.paxle.gui.impl.log.ILogReader"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/log/log4j"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="true" type="Boolean"
 * @scr.property name="logreader.type" value="log4j"
 */
public class Log4jReader extends ALayoutServlet implements ILogReader {
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
	
	@Override
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		try {
			final String action = request.getParameter("action");
			if (action != null) {
				if (action.equals("download")) {
					// getting the logfile to download
					final String fileName = request.getParameter("file");
					final File file = this.findLogFile(fileName);
					
					response.setHeader("Content-Type","text/plain");
					InputStream fileInput = null;
					try {
						fileInput = new BufferedInputStream(new FileInputStream(file));
						OutputStream clientOut = response.getOutputStream();
						
						// copy data
						IOUtils.copy(fileInput, clientOut);
					} finally {
						if (fileInput != null) fileInput.close();
					}
				}
			} else {		
				super.doRequest(request, response);
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}	
	}
		
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		// loop throug all file-appenders
		for (FileAppender fileAppender : this.getFileAppenders()) {
			try {
				// getting the basic logfile name
				final String logFileName = fileAppender.getFile();					
				final File logFile = new File(logFileName).getCanonicalFile();
				final File logDir = logFile.getParentFile();
				final String logFileNameFilter = logFile.getName();
				context.put("logDir", logDir);

				// getting all files including the rotated logfiles					
				final FileFilter logFileFilter = new WildcardFileFilter(logFileNameFilter + "*");		
				File[] logFiles = logDir.listFiles(logFileFilter);
				Arrays.sort(logFiles);
				context.put("logfiles", logFiles);
			} catch (IOException e) {
				this.logger.error("Unexpected error while reading log4j-configuration",e);
			}
		}
	}
	
	protected File findLogFile(String fileName) throws IOException {
		// loop throug all file-appenders
		for (FileAppender fileAppender : this.getFileAppenders()) {
			// getting all files of this appender
			File[] logFiles = this.getLogFiles(fileAppender);
			if (logFiles != null) {
				for (File logFile : logFiles) {
					if (logFile.getName().equals(fileName)) {
						return logFile;
					}
				}
			}
		}
		return null;
	}
	
	public File[] getLogFiles(FileAppender fileAppender) throws IOException {
		final String logFileName = fileAppender.getFile();					
		final File logFile = new File(logFileName).getCanonicalFile();
		final File logDir = logFile.getParentFile();

		// getting all files including the rotated logfiles					
		final FileFilter logFileFilter = new WildcardFileFilter(logFile.getName() + "*");		
		File[] logFiles = logDir.listFiles(logFileFilter);
		Arrays.sort(logFiles);
		return logFiles;
	}
	
	public List<FileAppender> getFileAppenders() {
		ArrayList<FileAppender> fileAppenders = new ArrayList<FileAppender>();
		
		// getting the Log4j root logger
		Logger rootLogger = Logger.getRootLogger();
		
		// getting all appenders
		@SuppressWarnings("unchecked")
		Enumeration<Appender> appenders = rootLogger.getAllAppenders();
		while(appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			
			// searching for the file-appender
			if (appender instanceof FileAppender) {
				fileAppenders.add((FileAppender)appender);
			}
		}		
		
		return fileAppenders;
	}
	
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/templates/LogViewLog4j.vm");
	}
	
	@SuppressWarnings("unchecked")
	public LogData getLogData() {
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
	
	private static class Entry implements LogDataEntry {
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