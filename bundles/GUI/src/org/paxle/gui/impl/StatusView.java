package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;


public class StatusView extends VelocityServlet {
	private ServiceManager manager = null;
	
	public StatusView(ServiceManager manager) {
		this.manager = manager;
	}
	
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {

        Template template = null;

        try {
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
