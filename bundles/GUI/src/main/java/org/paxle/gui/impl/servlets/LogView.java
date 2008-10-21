/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.gui.impl.servlets;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.service.log.LogService;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.Log4jMemoryAppender;
import org.paxle.gui.impl.ServiceManager;

public class LogView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	private final Log4jMemoryAppender log4jAppender;
	
	public LogView() {
		// creating a custom appender to intercept log4j events
		this.log4jAppender = new Log4jMemoryAppender();
		
		// getting the Log4j root logger
		Logger rootLogger = Logger.getRootLogger();
		
		// append our custom in memory appender
		rootLogger.addAppender(log4jAppender);
	}
	
	@Override
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context)
	{
		Template template = null;
		
		try {
			if( request.getParameter("filterLogLevel") != null) {
				context.put( "filterLogLevel", new Integer(request.getParameter( "filterLogLevel")));
			} else {
				context.put( "filterLogLevel", Integer.valueOf(4));
			}
			
			if(request.getParameter("logType") == null || request.getParameter("logType").equals("log4j")) {
				context.put("logType", "log4j");
				context.put("logReader",this.log4jAppender);
			} else {
				context.put("logType", request.getParameter( "logType"));
				context.put("logReader",((ServiceManager)context.get("manager")).getService("org.osgi.service.log.LogReaderService"));
			}
			
			//HashMap to determine LogLevelName
			HashMap<Integer, String> logLevelName = new HashMap<Integer, String>();
			logLevelName.put(Integer.valueOf(LogService.LOG_ERROR) , "error");
			logLevelName.put(Integer.valueOf(LogService.LOG_WARNING), "warn");
			logLevelName.put(Integer.valueOf(LogService.LOG_INFO), "info");
			logLevelName.put(Integer.valueOf(LogService.LOG_DEBUG), "debug");
			context.put( "logLevelNames" , logLevelName);
			
			final String type = request.getParameter("type");
			if (type == null || type.equals("default")) {
				// nothing to do
			} else if (type.equals("plain")) {
				context.put("layout", "plain.vm");
				context.put("type", type);
			}
			
			template = this.getTemplate("/resources/templates/LogView.vm");
		} catch( Exception e ) {
			System.err.println("Exception caught: " + e.getMessage());
		} catch (Error e) {
			e.printStackTrace();
		}
		
		return template;
	}
}
