package org.paxle.gui.impl.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServiceManager;

public class SysDown extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context)
	{
		Template template = null;
		IServiceManager manager = (IServiceManager) context.get(SERVICE_MANAGER);
		
		try {
			
			int shutdownDelay = 5;
			
			// If restart is true, restart, in any other case simply shut down
			if (request.getParameter("restart") != null && request.getParameter("restart").equalsIgnoreCase("true")) {
				manager.restartFrameworkDelayed(shutdownDelay);
				context.put("restart", Boolean.TRUE);
			} else {
				manager.shutdownFrameworkDelayed(shutdownDelay);
				context.put("restart", Boolean.FALSE);
			}
			
			template = this.getTemplate("/resources/templates/SysDown.vm");
		} catch( Exception e ) {
			System.err.println("Exception caught: " + e.getMessage());
		}
		
		return template;
	}
	
}
