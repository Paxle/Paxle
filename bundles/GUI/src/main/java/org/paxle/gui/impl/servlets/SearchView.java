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
package org.paxle.gui.impl.servlets;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServiceManager;
import org.paxle.gui.IServletManager;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchRequest;
import org.paxle.se.search.ISearchResult;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/search"
 * @scr.property name="org.paxle.servlet.menu" value="%menu.search"
 * @scr.property name="org.paxle.servlet.menu.pos" value="0" type="Integer"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="false" type="Boolean"
 */
public class SearchView extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String SEPROVIDER_MANAGER = "org.paxle.se.search.ISearchProviderManager";
	public static final String PARAM_FORMAT = "format";
	public static final String PARAM_QUERY = "query";
	public static final String PARAM_SE_PROVIDERS = "seProviders";	
	
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {

		String format = request.getParameter(PARAM_FORMAT);
		try {
			IServiceManager manager = (IServiceManager) context.get(SERVICE_MANAGER);
			
			if (request.getParameter(PARAM_QUERY) != null && !request.getParameter(PARAM_QUERY).equals("")) {
				final String query = request.getParameter(PARAM_QUERY);
				final String[] allowedProviderIDs = request.getParameterValues(PARAM_SE_PROVIDERS);
				context.put("searchQuery", query);
				context.put(PARAM_SE_PROVIDERS,allowedProviderIDs);
				
				if (!manager.hasService(SEPROVIDER_MANAGER)) {
					context.put("seBundleNotInstalled", Boolean.TRUE);
				} else {
					try {
						final ISearchProviderManager searchProviderManager = (ISearchProviderManager) manager.getService(SEPROVIDER_MANAGER);
						final int maxResults = 50;
						final long timeout = 10000l;
						
						// creating a search request object
						ISearchRequest searchRequest = searchProviderManager.createRequest(query, maxResults, timeout);
						if (allowedProviderIDs != null && allowedProviderIDs.length > 0) {
							// restricting the search request to a given set of search-providers
							searchRequest.setProviderIDs(Arrays.asList(allowedProviderIDs));
						}
						
						// starting the search
						logger.info("invoking search with '" + query + "', max results: " + maxResults + ", timeout: " + timeout);
						List<ISearchResult> searchResultList = searchProviderManager.search(searchRequest);
						
						int resultCount = 0;
						for (ISearchResult result : searchResultList) resultCount += result.getSize();
						
						context.put("searchResultList", searchResultList);
						context.put("searchResultCount", Integer.valueOf(resultCount));
					} catch (Exception e) {
						final Throwable cause = e.getCause();
						final String msg;
						if (cause == null) {
							msg = e.getMessage();
							logger.error("Error processing '" + query + "': " + msg, e);
						} else {
							msg = cause.getMessage();
							if (cause.getClass().getName().equals("org.paxle.se.search.SearchException")) {
								logger.error("SearchException processing '" + query + "': " + cause.getMessage());
							} else {
								logger.error("Error processing '" + query + "': " + cause.getMessage(), e);
							}
						}
						context.put("searchError", "Error processing query '" + query + "': " + msg);
					}
				}
			}
			
			/*
			 * Add required classes into the context
			 */
			// context.put("Search", manager.getService("org.paxle.se.search.ISearchProviderManager"));
			context.put("fieldManager", manager.getService("org.paxle.se.index.IFieldManager"));
			
			// get the servlet-manager and determine if the favicon-servlet was installed
			IServletManager servletManager = (IServletManager) manager.getService(IServletManager.class.getName());
			context.put("showFavicons", servletManager == null ? Boolean.FALSE : Boolean.valueOf(servletManager.hasServlet("/favicon")));
			
			//We need this to format the days in the RSS RFC-822 compliant (example: "Wed, 02 Oct 2002 15:00:00 +0200")
			context.put("localeUS", new Locale("us"));
			
			// add current context into itself (needed for render-tool)
			context.put("ctx",context);
			
			/*
			 * Choose the output format to use
			 */
			if (format != null && format.equalsIgnoreCase("rss")) {
				// we need to choose a different layout 
				context.put("layout", "plain.vm");
			}
		} catch (Exception e) {
			this.logger.error("Error",e);
		}
	}
	
	/**
	 * Choosing the template to use 
	 */
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		final String format = request.getParameter(PARAM_FORMAT);
		
		/*
		 * Choose the output format to use
		 */
		if (format != null && format.equalsIgnoreCase("rss")) {
			return this.getTemplate("/resources/templates/SearchViewRss.vm");
		} else {
			return this.getTemplate("/resources/templates/SearchViewHtml.vm");
		}
	}

	/**
	 * Configuring the content type to return
	 */
    protected void setContentType(HttpServletRequest request, HttpServletResponse response) {
    	final String format = request.getParameter(PARAM_FORMAT);
    	if (format != null && format.equalsIgnoreCase("rss")) {
			// setting the proper mimetype + charset
			response.setContentType("application/rss+xml; charset=utf-8");
    	} else {
    		// using the default content-type
    		super.setContentType(request, response);
    	}
    }
}
