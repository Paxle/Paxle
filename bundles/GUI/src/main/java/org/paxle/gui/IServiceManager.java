package org.paxle.gui;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public interface IServiceManager {

	public String getProperty(final String name);

	public void shutdownFramework() throws BundleException;
	
	/** Shuts down the OSGi framework after the given time (in seconds), but returns immediately */
	public void shutdownFrameworkDelayed(int delay) throws BundleException;

	public void restartFramework() throws BundleException;
	
	/** Shuts down and restarts the OSGi framework after the given time (in seconds), but returns immediately */
	public void restartFrameworkDelayed(int delay) throws BundleException;

	public Object getService(String serviceName);

	public ServiceReference[] getServiceReferences(String serviceName)
			throws InvalidSyntaxException;

	public Object getServiceProperty(String serviceName, String propertyname);

	public boolean hasService(String serviceName);

	public Object[] getServices(String serviceName, String query)
			throws InvalidSyntaxException;

	public boolean hasService(String serviceName, String query)
			throws InvalidSyntaxException;

	public Bundle[] getBundles();

	public Bundle getBundle(long bundleID);

}