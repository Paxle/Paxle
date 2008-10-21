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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;

public class StatusView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public Template handleRequest( 
			HttpServletRequest request,
			HttpServletResponse response,
			Context context 
			) {
		Template template = null;
		try {        	
			if (request.getParameter("pauseCrawl") != null) {
				context.put("doPause", Boolean.TRUE);
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (request.getParameter("resumeCrawl") != null) {
				context.put("doResume", Boolean.TRUE);
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (request.getParameter("processNextCrawl") != null) {
				context.put("doProcessNextCrawl", Boolean.TRUE);
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} 

			if (request.getParameter("shutdown") != null) {
				response.sendRedirect("/sysdown?restart=false");
			} else if (request.getParameter("restart") != null) {
				response.sendRedirect("/sysdown?restart=true");
			} else {
				// adding servlet to context
				context.put("statusView",this);
				context.put("servletContext", this.getServletConfig().getServletContext());
				
				/*
				 * Setting template parameters
				 */             
				template = this.getTemplate("/resources/templates/StatusView.vm");
			}
		} catch( Exception e ) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return template;
	}
	
	public Object getCacheManager() {
		try {
			return Thread.currentThread().getContextClassLoader()
			.loadClass("net.sf.ehcache.CacheManager")
			.getMethod("getInstance", (Class[]) null)
			.invoke(null, (Object[]) null);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}			
	}
}
