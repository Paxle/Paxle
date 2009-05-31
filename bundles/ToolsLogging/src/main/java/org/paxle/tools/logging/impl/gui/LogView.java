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
package org.paxle.tools.logging.impl.gui;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.service.log.LogService;
import org.paxle.gui.ALayoutServlet;
import org.paxle.tools.logging.ILogReader;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/log"
 * @scr.property name="org.paxle.servlet.menu" value="%menu.info/%menu.info.log"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="false" type="Boolean"
 * @scr.property name="org.paxle.servlet.menu.icon" value="/resources/images/script.png"
 */
public class LogView extends ALayoutServlet {	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Log4j appender to intercept log4j messages
	 * @scr.reference target="(org.paxle.tools.logging.ILogReader.type=log4j)"
	 */
	protected ILogReader log4jAppender;
	
	/**
	 * @scr.reference target="(org.paxle.tools.logging.ILogReader.type=osgi)"
	 */
	protected ILogReader osgiAppender;
	
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		try {
			if( request.getParameter("filterLogLevel") != null) {
				context.put( "filterLogLevel", new Integer(request.getParameter( "filterLogLevel")));
			} else {
				context.put( "filterLogLevel", Integer.valueOf(LogService.LOG_DEBUG));
			}
			
			if(request.getParameter("logType") == null || request.getParameter("logType").equals("log4j")) {
				context.put("logType", "log4j");
				context.put("logReader",this.log4jAppender);
			} else {
				context.put("logType", request.getParameter("logType"));
				context.put("logReader",this.osgiAppender);
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
			
			context.put("logView", this);
		} catch(Throwable e) {
			this.logger.error(e);
		}
	}
	
	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		final String type = request.getParameter("type");
		if (type != null && type.equals("plain")) {
			return this.getTemplate("/resources/templates/LogViewPlain.vm");
		} else {
			return this.getTemplate("/resources/templates/LogViewHtml.vm");
		}
	}
	
	@Override
	protected void setContentType(HttpServletRequest request, HttpServletResponse response) {
		final String type = request.getParameter("type");
		if (type != null && type.equals("plain")) {
			response.setContentType("text/plain; charset=UTF-8");
		} else {
			super.setContentType(request, response);
		}
	}	
	
	/**
	 * Function to convert a {@link Throwable} into a string
	 */
	public String toString(Throwable e) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			PrintStream errorOut = new PrintStream(bout,false,"UTF-8");
			e.printStackTrace(errorOut);

			errorOut.flush();
			errorOut.close();
			return bout.toString("UTF-8");
		} catch (Exception ex) {
			// should not occur
			this.logger.error(e);
			return null;
		}
	}
}
