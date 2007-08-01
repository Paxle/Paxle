package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;


public class CrawlerView extends VelocityViewServlet {
	private ServiceManager manager = null;
    private VelocityEngine velocity = null;
	
	public CrawlerView(ServiceManager manager, VelocityEngine velocity) {
		this.manager = manager;
        this.velocity = velocity;
	}
	
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {

        Template template = null;

        try {
        	if (request.getParameter("startURL") != null) {
        		Object[] sinks = this.manager.getServices(IDataSink.class.getName(), "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)");
        		if (sinks != null) {
        			Command cmd = new Command();
        			cmd.setLocation(request.getParameter("startURL"));
        			((IDataSink)sinks[0]).putData(cmd);
        		}
        	}
        	
        	/*
        	 * Setting template parameters
        	 */        	
            context.put("manager", this.manager);     
            
            template = this.velocity.getTemplate("/resources/templates/crawler.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        }

        return template;
    }

}
