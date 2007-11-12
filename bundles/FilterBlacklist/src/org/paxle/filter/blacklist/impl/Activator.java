package org.paxle.filter.blacklist.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.blacklist.BlacklistServlet;

public class Activator implements BundleActivator {

    private static BundleContext bc;
    private static HttpService http;
      
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        bc = context;
        
        File list = bc.getDataFile("../../../blacklist");
        list.mkdirs();
        new File(list, "default.list");
        
        Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
        filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in", "org.paxle.parser.out"});
        bc.registerService(IFilter.class.getName(), new BlacklistFilter(list), filterProps);
        
        ServiceReference sr = bc.getServiceReference(HttpService.class.getName());
        http = (HttpService) bc.getService(sr);
        if(http != null) {  
            http.registerServlet("/blacklist", new BlacklistServlet(), null, null);
        }
}

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
      bc = null;
  }
}