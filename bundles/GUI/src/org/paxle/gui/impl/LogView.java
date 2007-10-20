package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.service.log.LogService;

public class LogView extends AServlet {

    private static final long serialVersionUID = 1L;

    public LogView(ServiceManager manager) {
		super(manager);
	}	
	
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {

        Template template = null;

        try {
        	/*
        	 * Setting template parameters
        	 */        	        	   
            context.put("LEVEL_ERROR",Integer.toString(LogService.LOG_ERROR));
            context.put("LEVEL_WARNING",Integer.toString(LogService.LOG_WARNING));
            context.put("LEVEL_INFO",Integer.toString(LogService.LOG_INFO));
            context.put("LEVEL_DEBUG",Integer.toString(LogService.LOG_DEBUG));
            
            template = this.getTemplate("/resources/templates/LogView.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        }

        return template;
    }

}
