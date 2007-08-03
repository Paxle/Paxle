package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;


public class StatusView extends AServlet {
	
	public StatusView(ServiceManager manager) {
		super(manager);
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
            template = this.getTemplate("/resources/templates/status.vm");
        } catch( Exception e ) {
        	e.printStackTrace();
        } catch (Error e) {
        	e.printStackTrace();
        }

        return template;
    }

}
