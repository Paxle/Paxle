package org.paxle.gui.impl.servlets;

import java.io.BufferedReader;
import java.io.StringReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.data.IDataSink;
import org.paxle.core.queue.Command;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

public class CrawlerView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;	

    public CrawlerView(String bundleLocation) {
		super(bundleLocation);
	}
	
    public Template handleRequest( 
    		HttpServletRequest request,
            HttpServletResponse response,
            Context context 
    ) {

        Template template = null;

        try {
        	ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
        	
        	if (request.getParameter("startURL") != null) {
        		Object[] sinks = manager.getServices(IDataSink.class.getName(), "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)");
        		if (sinks != null) {
                    if (!request.getParameter("startURL").equals(""))
                        ((IDataSink)sinks[0]).putData(Command.createCommand(request.getParameter("startURL")));
        		}
        	}
            else if (request.getParameter("startURL2") != null) {
                Object[] sinks = manager.getServices(IDataSink.class.getName(), "(org.paxle.core.data.IDataSink.id=org.paxle.crawler.sink)");
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
            template = this.getTemplate("/resources/templates/crawler.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
          e.printStackTrace();
        }

        return template;
    }

}