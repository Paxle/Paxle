package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;


public class CrawlerView extends AServlet {

	public CrawlerView(ServiceManager manager) {
		super(manager);
	}		
	
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {

        Template template = null;

        try {
        	if (request.getParameter("startURL") != null) {
        		Object[] sinks = this.manager.getServices(IDataSink.class.getName(), "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)");
        		if (sinks != null) {
        			((IDataSink)sinks[0]).putData(Command.createCommand(request.getParameter("startURL")));
        		}
        	}
            else if (request.getParameter("startURL2") != null) {
                Object[] sinks = this.manager.getServices(IDataSink.class.getName(), "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)");
                if (sinks != null) {
                    String startURLs = request.getParameter("startURL2");
                    String [] URLs = startURLs.split("\r\n");
                    for (int i=0;i<URLs.length;i++) {
                        ((IDataSink)sinks[0]).putData(Command.createCommand(URLs[i]));
                    }
                }
            }
        	/*
        	 * Setting template parameters
        	 */        	
            context.put("manager", this.manager);     
            
            template = this.getTemplate("/resources/templates/crawler.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        }

        return template;
    }

}
