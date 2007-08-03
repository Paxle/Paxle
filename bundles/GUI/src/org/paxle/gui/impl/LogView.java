package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
import org.osgi.service.log.LogService;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;


public class LogView extends VelocityViewServlet {
	private ServiceManager manager = null;
    private VelocityEngine velocity = null;
	
	public LogView(ServiceManager manager, VelocityEngine velocity) {
		this.manager = manager;
        this.velocity = velocity;
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
            context.put("LEVEL_ERROR",Integer.toString(LogService.LOG_ERROR));
            context.put("LEVEL_WARNING",Integer.toString(LogService.LOG_WARNING));
            context.put("LEVEL_INFO",Integer.toString(LogService.LOG_INFO));
            context.put("LEVEL_DEBUG",Integer.toString(LogService.LOG_DEBUG));
            
            template = this.velocity.getTemplate("/resources/templates/LogView.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        }

        return template;
    }

}
