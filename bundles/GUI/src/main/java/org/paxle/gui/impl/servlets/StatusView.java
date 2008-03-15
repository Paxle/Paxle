
package org.paxle.gui.impl.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

public class StatusView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
	public StatusView(String bundleLocation) {
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
        	
    		if (request.getParameter("pauseCrawl") != null) {
    			context.put("doPause", Boolean.TRUE);
    		} else if (request.getParameter("resumeCrawl") != null) {
    			context.put("doResume", Boolean.TRUE);
    		} else if (request.getParameter("processNextCrawl") != null) {
    			context.put("doProcessNextCrawl", Boolean.TRUE);
    		} 
        	
            if (request.getParameter("shutdown") != null) {
        		manager.shutdownFramework();
        	} else if (request.getParameter("restart") != null) {
        		manager.restartFramework();
        	} else {
        	   	/*
	        	 * Setting template parameters
	        	 */             
	            template = this.getTemplate("/resources/templates/StatusView.vm");
        	}
        } catch( Exception e ) {
        	e.printStackTrace();
        } catch (Error e) {
        	e.printStackTrace();
        }

        return template;
    }

}
