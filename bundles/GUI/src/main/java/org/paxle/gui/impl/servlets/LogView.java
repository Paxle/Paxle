package org.paxle.gui.impl.servlets;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.service.log.LogService;
import org.paxle.gui.ALayoutServlet;

public class LogView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context)
	{
        Template template = null;

        try {
        	
        	if( request.getParameter( "filterLogLevel") != null) {
        		context.put( "filterLogLevel", new Integer( request.getParameter( "filterLogLevel")));
        	}
        	else {
        		context.put( "filterLogLevel", Integer.valueOf(4));
        	}
        	
        	//HashMap to determine LogLevelName
        	HashMap<Integer, String> level = new HashMap<Integer, String>();
        	level.put( Integer.valueOf(LogService.LOG_ERROR) , "error");
        	level.put( Integer.valueOf(LogService.LOG_WARNING), "warning");
        	level.put( Integer.valueOf(LogService.LOG_INFO), "info");
        	level.put( Integer.valueOf(LogService.LOG_DEBUG), "debug");
        	context.put( "logLevelName" , level);
            
            template = this.getTemplate("/resources/templates/LogView.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        } catch (Error e) {
        	e.printStackTrace();
        }

        return template;
	}

}
