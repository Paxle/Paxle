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

package org.paxle.tools.logging.impl.gui;

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
import java.util.zip.GZIPOutputStream;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityLayoutServlet;

@Component(metatype=false, immediate=true,
		label="Logging Servlet",
		description="A Servlet to display log-messages"
)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/log/log4j"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=true)
})
public class Log4jView extends VelocityLayoutServlet implements Servlet {
	private static final long serialVersionUID = 1L;	
	
	private static final String ACTION_GET_LEVEL = "getLevel";
	private static final String ACTION_SET_LEVEL = "setLevel";
	private static final String ACTION_DELETE = "delete";
	private static final String ACTION_DOWNLOAD = "download";	
	
	private static final String PARAM_LEVEL = "level";
	private static final String PARAM_LOGGER = "logger";
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_FORMAT = "format";
	private static final String PARAM_FILE = "file";

    /**
     * Logger
     */
    protected Log logger = LogFactory.getLog(this.getClass());		
	
	@Override
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		try {
			final String action = request.getParameter(PARAM_ACTION);
			if (action != null) {
				final String fileName = request.getParameter(PARAM_FILE);
				final File file = (fileName==null)?null: this.findLogFile(fileName);
				
				if (action.equals(ACTION_DOWNLOAD)) {
					// getting the file-format
					final String format = request.getParameter(PARAM_FORMAT);
					if (format != null && format.equals("gzip")) {
						response.setContentType("application/gzip");
						response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".gz");
					} else {					
						response.setHeader("Content-Type","text/plain");
					}
					
					InputStream fileInput = null;
					try {					
						// create I/O streams
						fileInput = new BufferedInputStream(new FileInputStream(file));
						OutputStream clientOut = null;
						if (format != null && format.equals("gzip")) {
							clientOut = new GZIPOutputStream(response.getOutputStream());
						} else {
							clientOut = response.getOutputStream();
						}
						
						// copy data
						IOUtils.copy(fileInput, clientOut);
						clientOut.flush();
						
						// finish gzip stream
						if (clientOut instanceof GZIPOutputStream) {
							((GZIPOutputStream)clientOut).finish();
						}
						
					} finally {
						if (fileInput != null) fileInput.close();
					}
				} else if (action.equals(ACTION_DELETE)) {
					// deleting the logfile
					file.delete();
					response.sendRedirect(request.getServletPath());
				} else if (action.equals(ACTION_SET_LEVEL)) {
					final String loggerName = request.getParameter(PARAM_LOGGER);
					final String loggerLevel = request.getParameter(PARAM_LEVEL);
					final Logger theLogger = Logger.getLogger(loggerName);
					theLogger.setLevel(Level.toLevel(loggerLevel));
					
					response.sendRedirect(request.getServletPath() + "#dlogconfig");
				} else {		
					super.doRequest(request, response);
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
				
				// calculating total size
				long totalSize = 0;
				for (File nextLogFile : logFiles) totalSize += nextLogFile.length();
				context.put("totalSize",Long.valueOf(totalSize));				
			} catch (IOException e) {
				this.logger.error("Unexpected error while reading log4j-configuration",e);
			}
		}
		
		final String action = request.getParameter(PARAM_ACTION);
		if (action != null) {
			final String loggerName = request.getParameter(PARAM_LOGGER);
			if (action.equals(ACTION_GET_LEVEL)) {
				final Logger theLogger = Logger.getLogger(loggerName);
				context.put("loggerName", loggerName);
				context.put("loggerLevel", theLogger.getEffectiveLevel());
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
}
