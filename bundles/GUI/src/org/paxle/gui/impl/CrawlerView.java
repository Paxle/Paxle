package org.paxle.gui.impl;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;

public class CrawlerView extends AServlet {

    private static final long serialVersionUID = 1L;

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
                    if (!request.getParameter("startURL").equals(""))
                        ((IDataSink)sinks[0]).putData(Command.createCommand(request.getParameter("startURL")));
        		}
        	}
            else if (request.getParameter("startURL2") != null) {
                Object[] sinks = this.manager.getServices(IDataSink.class.getName(), "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)");
                if (sinks != null) {
                    BufferedReader startURLs = new BufferedReader(new StringReader(request.getParameter("startURL2")));
                    String line;
                    while ((line = startURLs.readLine()) != null) {
                        if (line.equals("")) continue;
                        ((IDataSink)sinks[0]).putData(Command.createCommand(line));
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
          e.printStackTrace();
        }

        return template;
    }

}
