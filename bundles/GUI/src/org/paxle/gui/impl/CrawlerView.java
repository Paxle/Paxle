package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;


public class CrawlerView extends VelocityServlet {
	private ServiceManager manager = null;
	
	public CrawlerView(ServiceManager manager) {
		this.manager = manager;
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
            
            template = Velocity.getTemplate("/resources/templates/crawler.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        }

        return template;
    }

}
