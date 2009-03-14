/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.gui.impl;

import java.util.ArrayList;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.paxle.gui.IServiceManager;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class ServiceManager implements IServiceManager {
	@SuppressWarnings("MS_CANNOT_BE_FINAL")
	public static BundleContext context = null;
    
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
		String osgiFrameworkVendor = context.getProperty(Constants.FRAMEWORK_VENDOR);
		if (osgiFrameworkVendor.equalsIgnoreCase("Eclipse")) {
			// try to find an application launcher and shutdown
			Object app = this.getService("org.eclipse.osgi.service.runnable.ApplicationLauncher");
			if (app != null) {
				try {
					app.getClass().getMethod("shutdown", (Class[])null).invoke(app, (Object[])null);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		Bundle framework = ServiceManager.context.getBundle(0);
		if (framework != null) {
			framework.stop();
		}
		
//		try {
//			Thread.sleep(20000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//		
//		// wait a few seconds, then try a System.exit
//		System.err.println("System.exit");
//		System.exit(0);
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
	
	public Object getService(ServiceReference ref) {
		return context.getService(ref);
	}
    
	/* (non-Javadoc)
	 * @see org.paxle.gui.impl.IServiceManager#hasSerivce(java.lang.String, java.lang.String)
	 */
	public boolean hasService(String serviceName, String query) throws InvalidSyntaxException {
		Object[] services = this.getServices(serviceName, query);
		return (services != null && services.length > 0);
	}
	
    /**
     * Get all currently installed {@link Bundle OGSi-bundles}
     */
    public Bundle[] getBundles() {
        return ServiceManager.context.getBundles();
    }

    /**
     * Get {@link Bundle OGSi-bundle} by {@link Bundle#getBundleId() ID}
     */
    public Bundle getBundle(long bundleID) {
        return ServiceManager.context.getBundle(bundleID);
    }
    
    public Bundle getBundle(String bundleSymbolicName) {
    	if (bundleSymbolicName == null) throw new NullPointerException("The symbolic name was null");
    	bundleSymbolicName = bundleSymbolicName.trim();
    	
    	Bundle[] bundles = this.getBundles();
    	if (bundles != null) {
    		for (Bundle bundle : bundles) {
    			String currentSymbolicName = bundle.getSymbolicName();
    			if (currentSymbolicName.equalsIgnoreCase(bundleSymbolicName)) return bundle;
    		}
    	}
    	return null;
    }
    
    /**
     * Get {@link Bundle OGSi-bundle} by {@link Bundle#getSymbolicName() symbolic-name}
     */
    public boolean hasBundle(String bundleSymbolicName) {
    	if (bundleSymbolicName == null) throw new NullPointerException("The symbolic name was null");    	    	
    	return this.getBundle(bundleSymbolicName) != null;
    }
    
    public Bundle[] getBundles(String filterString) throws InvalidSyntaxException {
    	Filter filter = ServiceManager.context.createFilter(filterString);
    	
    	ArrayList<Bundle> results = new ArrayList<Bundle>();
    	for (Bundle bundle : this.getBundles()) {
    		if (filter.match(bundle.getHeaders())) {
    			results.add(bundle);
    		}
    	}
    	
    	return results.toArray(new Bundle[results.size()]);
    }

	/* (non-Javadoc)
	 * @see org.paxle.gui.IServiceManager#shutdownFrameworkDelayed(int)
	 */
	public void shutdownFrameworkDelayed(int delay) throws BundleException {
		new SysDownDelayThread(delay, false, this);
	}

	/* (non-Javadoc)
	 * @see org.paxle.gui.IServiceManager#restartFrameworkDelayed()
	 */
	public void restartFrameworkDelayed(int delay) throws BundleException {
		new SysDownDelayThread(delay, true, this);
	}
}
