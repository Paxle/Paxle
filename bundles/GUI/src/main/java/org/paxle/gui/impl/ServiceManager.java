package org.paxle.gui.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.paxle.gui.IServiceManager;

public class ServiceManager implements IServiceManager {
	public static BundleContext context = null;
	
    /**
     * Default constructor.
     */
    public ServiceManager() {
        // do nothing
    }
    
    /* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#getProperty(java.lang.String)
	 */
    public String getProperty(final String name) {
    	return ServiceManager.context.getProperty(name);
    }
	
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#shutdownFramework()
	 */
	public void shutdownFramework() throws BundleException {
		Bundle framework = ServiceManager.context.getBundle(0);
		if (framework != null) {
			framework.stop();
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		// wait a view seconds, then try a System.exit
		System.err.println("System.exit");
		System.exit(0);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#restartFramework()
	 */
	public void restartFramework() throws BundleException {
		Bundle framework = ServiceManager.context.getBundle(0);
		if (framework != null) {
			framework.update();
		}		
	}

	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#getService(java.lang.String)
	 */
	public Object getService(String serviceName) {
		ServiceReference reference = ServiceManager.context.getServiceReference(serviceName);
		return (reference == null) ? null : ServiceManager.context.getService(reference);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#getServiceReferences(java.lang.String)
	 */
	public ServiceReference[] getServiceReferences(String serviceName) throws InvalidSyntaxException {
		return ServiceManager.context.getServiceReferences(serviceName,null);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#getServiceProperty(java.lang.String, java.lang.String)
	 */
	public Object getServiceProperty(String serviceName, String propertyname) {
		ServiceReference reference = ServiceManager.context.getServiceReference(serviceName);
		return (reference == null) ? null : reference.getProperty(propertyname);
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#hasService(java.lang.String)
	 */
	public boolean hasService(String serviceName) {
		return ServiceManager.context.getServiceReference(serviceName) != null;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#getServices(java.lang.String, java.lang.String)
	 */
	public Object[] getServices(String serviceName, String query) throws InvalidSyntaxException {
		ServiceReference[] references = ServiceManager.context.getServiceReferences(serviceName,query);
		if (references == null) return null;
		
		Object[] services = new Object[references.length];
		for (int i=0; i < references.length; i++) {			
			services[i] = ServiceManager.context.getService(references[i]);
		}
		
		return services;
	}
    
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#hasSerivce(java.lang.String, java.lang.String)
	 */
	public boolean hasSerivce(String serviceName, String query) throws InvalidSyntaxException {
		Object[] services = this.getServices(serviceName, query);
		return (services != null && services.length > 0);
	}
	
    /* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#getBundles()
	 */
    public Bundle[] getBundles() {
        return ServiceManager.context.getBundles();
    }

    /* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#getBundle(long)
	 */
    public Bundle getBundle(long bundleID) {
        return ServiceManager.context.getBundle(bundleID);
    }
}
