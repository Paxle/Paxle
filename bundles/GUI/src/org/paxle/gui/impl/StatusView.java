package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;


public class StatusView extends VelocityViewServlet {
	private ServiceManager manager = null;
	
	public StatusView(ServiceManager manager) {
		this.manager = manager;
	}
	
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {

        Template template = null;

        try {
        	if (request.getParameter("shutdown") != null) {
        		this.manager.shutdownFramework();
        	} else if (request.getParameter("restart") != null) {
        		this.manager.restartFramework();
        	}
        	
        	/*
        	 * Setting template parameters
        	 */
            context.put("manager", this.manager);     
            
            template = Velocity.getTemplate("/resources/templates/status.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        }

        return template;
    }

}
