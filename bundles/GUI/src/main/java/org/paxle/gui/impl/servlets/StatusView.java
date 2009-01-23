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
import org.osgi.framework.InvalidSyntaxException;
import org.paxle.core.IMWComponent;
import org.paxle.crawler.ISubCrawlerManager;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServiceManager;
import org.paxle.gui.IServletManager;
import org.paxle.parser.ISubParserManager;
import org.paxle.se.search.ISearchProviderManager;

public class StatusView extends ALayoutServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		try {
			final IServiceManager manager = this.getServiceManager();
			final IServletManager servletManager = (IServletManager) manager.getService(IServletManager.class.getName());
			
			if (request.getParameter("pauseCrawl") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// pause crawler
				IMWComponent<?> crawler = this.getCrawler();
				if (crawler != null) crawler.pause();
				
				// redirect to status page
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (request.getParameter("resumeCrawl") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// resume crawler
				IMWComponent<?> crawler = this.getCrawler();
				if (crawler != null) crawler.resume();
				
				// redirect to status page
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (request.getParameter("processNextCrawl") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// process next command
				IMWComponent<?> crawler = this.getCrawler();
				if (crawler != null) crawler.processNext();
				
				// redirect to status page
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (request.getParameter("shutdown") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// redirecting to shutdown-servlet
				response.sendRedirect(servletManager.getFullAlias("/sysctrl") + "?action=shutdown");
			} else if (request.getParameter("restart") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// redirecting to shutdown-servlet
				response.sendRedirect(servletManager.getFullAlias("/sysctrl") + "?action=restart");
			} else if (
				(request.getParameter("doEnableProtocol") != null)    || 
				(request.getParameter("doDisableProtocol") != null)
			){				
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;				
				
				// getting the crawler manager
				ISubCrawlerManager crawlerManager = (ISubCrawlerManager) manager.getService(ISubCrawlerManager.class.getName());
				String protocol = request.getParameter("protocol");
				
				// enable or disable protocol
				if (request.getParameter("doEnableProtocol") != null) {
					crawlerManager.enableProtocol(protocol);
				} else if (request.getParameter("doDisableProtocol") != null) {
					crawlerManager.disableProtocol(protocol);
				}
				
				// redirecting request
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (
				(request.getParameter("doEnableMimeType") != null)    || 
				(request.getParameter("doDisableMimeType") != null)
			){
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// getting the parser manager
				ISubParserManager parserManager = (ISubParserManager) manager.getService(ISubParserManager.class.getName());
				String mimeType = request.getParameter("mimeType");
				
				// enable or disable mimetype
				if (request.getParameter("doEnableMimeType") != null) {
					parserManager.enableMimeType(mimeType);
				} else if (request.getParameter("doDisableMimeType") != null) {
					parserManager.disableMimeType(mimeType);
				}
				
				// redirecting request
				response.sendRedirect(request.getServletPath() + "#dparser");
			} else if (
				(request.getParameter("doEnableSEProvider") != null)  || 
				(request.getParameter("doDisableSEProvider") != null)				
			) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// getting se-provider manager
				ISearchProviderManager seProviderManager = (ISearchProviderManager) manager.getService(ISearchProviderManager.class.getName());
				String seProvider = request.getParameter("seProvider");
				
				// enable or disable provider
				if (request.getParameter("doEnableSEProvider") != null) {
					seProviderManager.enableProvider(seProvider);
				} else if (request.getParameter("doDisableSEProvider") != null) {
					seProviderManager.disableProvider(seProvider);
				}
				
				// redirecting request
				response.sendRedirect(request.getServletPath() + "#dsearch");
			} else {		
				super.doRequest(request, response);
			}
		} catch (Throwable e) {
			this.logger.error(e);
		}		
	}
	
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		try {        	
			// adding servlet to context
			context.put("statusView",this);
			context.put("servletContext", this.getServletConfig().getServletContext());
		} catch (Throwable e) {
			this.logger.error(e);
		}
	}
	
	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/templates/StatusView.vm");
	}	
	
	private IMWComponent<?> getCrawler() throws InvalidSyntaxException {
		IServiceManager sm = this.getServiceManager();
		Object[] crawlers = sm.getServices("org.paxle.core.IMWComponent","(component.ID=org.paxle.crawler)");		
		if (crawlers == null && crawlers.length == 0) return null;
		return (IMWComponent<?>) crawlers[0];
	}
	
	/**
	 * @return a reference to the {@link net.sf.ehcache.CacheManager EhCache-Cachemanager}
	 */
	public Object getCacheManager() {
		try {
			return Thread.currentThread().getContextClassLoader()
				.loadClass("net.sf.ehcache.CacheManager")
				.getMethod("getInstance", (Class[]) null)
				.invoke(null, (Object[]) null);
		} catch (Throwable e) {
			this.logger.error(e);
			return null;
		}			
	}
}
