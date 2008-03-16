package org.paxle.gui.impl.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.osgi.service.log.LogService;
import org.paxle.gui.ALayoutServlet;

public class LogView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
    

    public LogView(String bundleLocation) {
		super(bundleLocation);
	}	
	
    public Template handleRequest( 
    		HttpServletRequest request,
            HttpServletResponse response,
            Context context 
    ) {

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
        } catch (Error e) {
        	e.printStackTrace();
        }

        return template;
    }

    @Override
    protected void mergeTemplate(Template template, Context context, HttpServletResponse response) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, IOException, UnsupportedEncodingException, Exception {
    	try {
    	// TODO Auto-generated method stub
    	super.mergeTemplate(template, context, response);
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    }
}
