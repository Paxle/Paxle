
package org.paxle.gui.impl.servlets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServletManager;
import org.paxle.gui.impl.ServiceManager;

public class SearchView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
	public Template handleRequest( 
			HttpServletRequest request,
			HttpServletResponse response,
			Context context 
	) {

		Template template = null;
		String format = request.getParameter("format");
		try {
			ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
			
			if (request.getParameter("query") != null && !request.getParameter("query").equals("")) {
				// context.put("searchQuery", request.getParameter("query"));
				final String query = request.getParameter("query");
				context.put("searchQuery", query);
				final Object isearchProviderManager = manager.getService("org.paxle.se.search.ISearchProviderManager");
				if (isearchProviderManager == null) {
					context.put("seBundleNotInstalled", Boolean.TRUE);
				} else {
					// reflection is used here to not create hard dependencies to the SearchEngine bundle
					final Method searchMethod = isearchProviderManager.getClass().getMethod("search", String.class, int.class, long.class);
					try {
						final int maxResults = 50;
						final long timeout = 10000l;
						
						logger.info("invoking search with '" + query + "', max results: " + maxResults + ", timeout: " + timeout);
						final Object searchResultList = searchMethod.invoke(
								isearchProviderManager, query, Integer.valueOf(maxResults), Long.valueOf(timeout));
						context.put("searchResultList", searchResultList);
					} catch (InvocationTargetException e) {
						final Throwable cause = e.getCause();
						final String msg;
						if (cause == null) {
							msg = e.getMessage();
							logger.error("Error processing '" + query + "': " + cause.getMessage(), e);
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
			
			// add current context into itself (needed for render-tool)
			context.put("ctx",context);
			
			/*
			 * Choose the output format to use
			 */
			if (format != null && format.equalsIgnoreCase("rss")) {
				// we need to choose a different layout 
				context.put("layout", "plain.vm");
				
				// setting the proper mimetype + charset
				response.setContentType("application/rss+xml; charset=utf-8");
				
				// the template to use
				template = this.getTemplate("/resources/templates/SearchViewRss.vm");
			} else {
				// the template to use
				template = this.getTemplate("/resources/templates/SearchViewHtml.vm");
			}
		} catch (Exception e) {
			this.logger.error("Error",e);
		}
		return template;
	}

}
