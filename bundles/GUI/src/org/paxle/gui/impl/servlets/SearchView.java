package org.paxle.gui.impl.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServletManager;
import org.paxle.gui.impl.ServiceManager;

public class SearchView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;

	public SearchView(String bundleLocation) {
		super(bundleLocation);
	}
	
	public Template handleRequest( 
			HttpServletRequest request,
			HttpServletResponse response,
			Context context 
	) {

		Template template = null;
		String format = request.getParameter("format");
		try {
			if (request.getParameter("query") != null && !request.getParameter("query").equals("")) {
				context.put("searchQuery", request.getParameter("query"));
			}
			
			/*
			 * Add required classes into the context
			 */
			ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
			context.put("Search", manager.getService("org.paxle.se.search.ISearchProviderManager"));
			context.put("fieldManager", manager.getService("org.paxle.se.index.IFieldManager"));
			
			// get the servlet-manager and determine if the favicon-servlet was installed
			IServletManager servletManager = (IServletManager) manager.getService(IServletManager.class.getName());
			context.put("showFavicons", servletManager == null ? Boolean.FALSE : servletManager.hasServlet("/favicon"));
			
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

	@Override
	protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException, UnsupportedEncodingException, Exception {
		// TODO Auto-generated method stub
		super.mergeTemplate(template, context, response);
	}
}
