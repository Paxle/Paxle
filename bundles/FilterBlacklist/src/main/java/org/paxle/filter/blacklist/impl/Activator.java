
package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.blacklist.BlacklistServlet;

public class Activator implements BundleActivator {

	/**
	 * A reference to the {@link BundleContext bundle-context}
	 */
    private static BundleContext bc;
      
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
    public void start(BundleContext context) throws Exception {
        bc = context;
        
//        File list = bc.getDataFile("../../../blacklist");
        File list = new File("blacklist");
        list.mkdirs();
        new File(list, "default.list");
        
        Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
        filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in", "org.paxle.parser.out"});
        bc.registerService(IFilter.class.getName(), new BlacklistFilter(list), filterProps);
        
//        ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
//        http = (HttpService) bc.getService(sr);
//        if(http != null) {  
//            http.registerServlet("/blacklist", new BlacklistServlet(), null, null);
//        }
                
        /* TODO:
        BlacklistServlet servlet = new BlacklistServlet();
        servlet.init(bc.getBundle().getEntry("/").toString(),"/blacklist");
        Hashtable<String, String> props = new Hashtable<String, String>();
        props.put("path", "/blacklist");
        props.put("menu", "Blacklist");
        bc.registerService("javax.servlet.Servlet", servlet, props);
        */
    }

    /**
     * This function is called by the osgi-framework to stop the bundle.
     * @see BundleActivator#stop(BundleContext)
     */		
    public void stop(BundleContext context) throws Exception {
    	bc = null;
    }
}