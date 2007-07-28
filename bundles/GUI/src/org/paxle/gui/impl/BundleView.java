package org.paxle.gui.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
import org.osgi.framework.Bundle;


public class BundleView extends VelocityViewServlet {
    private ServiceManager manager = null;
    
    public BundleView(ServiceManager manager) {
        this.manager = manager;
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
            
            template = Velocity.getTemplate("/resources/templates/bundle.vm");
        } catch( Exception e ) {
            System.err.println("Exception caught: " + e.getMessage());
        }
        
        return template;
    }
}
