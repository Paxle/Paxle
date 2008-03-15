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
    	states.put(Integer.valueOf(Bundle.ACTIVE), "active");
    	states.put(Integer.valueOf(Bundle.INSTALLED), "installed");
    	states.put(Integer.valueOf(Bundle.RESOLVED), "resolved");
    	states.put(Integer.valueOf(Bundle.STARTING), "starting");
    	states.put(Integer.valueOf(Bundle.STOPPING), "stopping");
    	states.put(Integer.valueOf(Bundle.UNINSTALLED), "uninstalled");
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
                Bundle bundle = manager.getBundle(Long.parseLong(request.getParameter("bundleID")));
                bundle.update();
            } else if (request.getParameter("start") != null) {
                Bundle bundle = manager.getBundle(Long.parseLong(request.getParameter("bundleID")));
                bundle.start();
            } else if (request.getParameter("stop") != null) {
                Bundle bundle = manager.getBundle(Long.parseLong(request.getParameter("bundleID")));
                bundle.stop();
            } else if (request.getParameter("restart") != null) {
                Bundle bundle = manager.getBundle(Long.parseLong(request.getParameter("bundleID")));
                bundle.stop();
                bundle.start();
            }else if(request.getParameter("uninstall") != null){
            	Bundle bundle = manager.getBundle(Long.parseLong(request.getParameter("bundleID")));
                bundle.uninstall();
            }else if(request.getParameter("install") != null && request.getParameter("bundlePath")!=null){
            	ServiceManager.context.installBundle(request.getParameter("bundlePath"));
            } else if (request.getParameter("details") != null) {
            	Bundle bundle = manager.getBundle(Long.parseLong(request.getParameter("bundleID")));
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
    			r.put(Long.valueOf(bundle.getBundleId()), bundle);
    	return r;
    }
}
