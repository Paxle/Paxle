package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.framework.Bundle;


public class BundleView extends AServlet {

	public BundleView(ServiceManager manager) {
		super(manager);
	}	
	
    public Template handleRequest( HttpServletRequest request,
            HttpServletResponse response,
            Context context ) {

        Template template = null;
        
        try {
            if (request.getParameter("update") != null) {
                Bundle bundle = this.manager.getBundle(Long.valueOf(request.getParameter("bundleID")));
                bundle.update();
            }
            context.put("manager", this.manager);             
            template = this.getTemplate("/resources/templates/bundle.vm");
        } catch( Exception e ) {
            System.err.println("Exception caught: " + e.getMessage());
        }
        
        return template;
    }
}
