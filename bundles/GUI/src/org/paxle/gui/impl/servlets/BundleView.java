package org.paxle.gui.impl.servlets;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;

import org.osgi.framework.Bundle;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.ServiceManager;

public class BundleView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
    private static final Map<Integer,String> states = new HashMap<Integer,String>();
    static {
    	states.put(Bundle.ACTIVE, "active");
    	states.put(Bundle.INSTALLED, "installed");
    	states.put(Bundle.RESOLVED, "resolved");
    	states.put(Bundle.STARTING, "starting");
    	states.put(Bundle.STOPPING, "stopping");
    	states.put(Bundle.UNINSTALLED, "uninstalled");
    }
    	
	public BundleView(String bundleLocation) {
		super(bundleLocation);
	}
	
    public Template handleRequest( HttpServletRequest request,
            HttpServletResponse response,
            Context context ) {
    	
        Template template = null;
        try {
            template = this.getTemplate("/resources/templates/bundle.vm");
            
            ServiceManager manager = (ServiceManager) context.get(SERVICE_MANAGER);
            if (request.getParameter("update") != null) {
                Bundle bundle = manager.getBundle(Long.valueOf(request.getParameter("bundleID")));
                bundle.update();
            } else if (request.getParameter("start") != null) {
                Bundle bundle = manager.getBundle(Long.valueOf(request.getParameter("bundleID")));
                bundle.start();
            } else if (request.getParameter("stop") != null) {
                Bundle bundle = manager.getBundle(Long.valueOf(request.getParameter("bundleID")));
                bundle.stop();
            } else if (request.getParameter("details") != null) {
            	Bundle bundle = manager.getBundle(Long.valueOf(request.getParameter("bundleID")));
            	context.put("bundle", bundle);
            }       
            context.put("bundles", bundles2map(manager.getBundles()));
            context.put("states", states);
        } catch (Exception e) {
            System.err.println("Exception caught: " + e.getMessage());
            e.printStackTrace();
        }
        
        return template;
    }
    
    private static TreeMap<Long,Bundle> bundles2map(Bundle[] bundles) {
    	final TreeMap<Long,Bundle> r = new TreeMap<Long,Bundle>();
    	for (Bundle bundle : bundles)
    		if (bundle.getBundleId() > 0)
    			r.put(bundle.getBundleId(), bundle);
    	return r;
    }
}
