
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
				response.sendRedirect(super.servletLocation + "#dcrawler");
			} else if (request.getParameter("resumeCrawl") != null) {
				context.put("doResume", Boolean.TRUE);
				response.sendRedirect(super.servletLocation + "#dcrawler");
			} else if (request.getParameter("processNextCrawl") != null) {
				context.put("doProcessNextCrawl", Boolean.TRUE);
				response.sendRedirect(super.servletLocation + "#dcrawler");
			} 

			if (request.getParameter("shutdown") != null) {
				response.sendRedirect("/sysdown?restart=false");
			} else if (request.getParameter("restart") != null) {
				response.sendRedirect("/sysdown?restart=true");
			} else {
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
}
