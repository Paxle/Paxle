package org.paxle.filter.index.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.paxle.core.filter.IFilter;

public class Activator implements BundleActivator {

    private static BundleContext bc;

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        bc = context;
        
        Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
        filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.indexer.in"});
        bc.registerService(IFilter.class.getName(), new OldIndexFilter(), filterProps);
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
    }
}