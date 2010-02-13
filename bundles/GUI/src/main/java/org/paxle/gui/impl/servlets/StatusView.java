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

package org.paxle.gui.impl.servlets;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.framework.InvalidSyntaxException;
import org.paxle.core.IMWComponent;
import org.paxle.crawler.ISubCrawlerManager;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServiceManager;
import org.paxle.parser.ISubParserManager;
import org.paxle.se.search.ISearchProviderManager;

@Component(metatype=false, immediate=true)
@Service(Servlet.class)
@Properties({
	@Property(name="org.paxle.servlet.path", value="/status"),
	@Property(name="org.paxle.servlet.doUserAuth", boolValue=false),
	@Property(name="org.paxle.servlet.menu", value="%menu.info/%menu.info.status"), 
	@Property(name="org.paxle.servlet.menu.icon", value="/resources/images/monitor.png")
})
public class StatusView extends ALayoutServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		try {
			final Context context = this.getVelocityView().createContext(request, response);
			final IServiceManager manager = (IServiceManager) context.get(IServiceManager.SERVICE_MANAGER);
			
			if (request.getParameter("pauseCrawl") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// pause crawler
				IMWComponent<?> crawler = this.getCrawler(manager);
				if (crawler != null) crawler.pause();
				
				// redirect to status page
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (request.getParameter("resumeCrawl") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// resume crawler
				IMWComponent<?> crawler = this.getCrawler(manager);
				if (crawler != null) crawler.resume();
				
				// redirect to status page
				response.sendRedirect(request.getServletPath() + "#dcrawler");
			} else if (request.getParameter("processNextCrawl") != null) {
				// check user authentication
				if (!this.isUserAuthenticated(request, response, true)) return;
				
				// process next command
				IMWComponent<?> crawler = this.getCrawler(manager);
				if (crawler != null) crawler.processNext();
				
				// redirect to status page
				response.sendRedirect(request.getServletPath() + "#dcrawler");
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
					// TODO: we need to replace this using the config-tool 
					// crawlerManager.enableProtocol(protocol);
				} else if (request.getParameter("doDisableProtocol") != null) {
					// TODO: we need to replace this using the config-tool
					// crawlerManager.disableProtocol(protocol);
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
	
	private IMWComponent<?> getCrawler(IServiceManager sm) throws InvalidSyntaxException {
		Object[] crawlers = sm.getServices("org.paxle.core.IMWComponent","(mwcomponent.ID=org.paxle.crawler)");		
		if (crawlers == null || crawlers.length == 0) return null;
		return (IMWComponent<?>) crawlers[0];
	}
	
	/**
	 * @return a reference to the {@link net.sf.ehcache.CacheManager EhCache-Cachemanager}
	 */
	public Object getCacheManager() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader == null) classLoader = this.getClass().getClassLoader();
			
			return classLoader
				.loadClass("net.sf.ehcache.CacheManager")
				.getMethod("getInstance", (Class[]) null)
				.invoke(null, (Object[]) null);
		} catch (Throwable e) {
			this.logger.error(e);
			return null;
		}			
	}
}
