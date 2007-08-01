package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;


public class StatusView extends VelocityViewServlet {
	
	private static final long serialVersionUID = 1L;
	
	private ServiceManager manager = null;
    private VelocityEngine velocity = null;
	
	public StatusView(ServiceManager manager, VelocityEngine velocity) {
		this.manager = manager;
        this.velocity = velocity;
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
            
            template = this.velocity.getTemplate("/resources/templates/status.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        }

        return template;
    }

}
