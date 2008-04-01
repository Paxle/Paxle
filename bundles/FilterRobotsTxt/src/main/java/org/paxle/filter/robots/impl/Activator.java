package org.paxle.filter.robots.impl;

import java.io.File;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.robots.IRobotsTxtManager;

public class Activator implements BundleActivator {
	private static String DB_PATH = "robots-db";	
	
	private RobotsTxtManager robotsTxtManager = null;
	private RobotsTxtCleanupThread robotsTxtCleanupThread = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */	
	public void start(BundleContext bc) throws Exception {
		this.robotsTxtManager = new RobotsTxtManager(new File(DB_PATH));
		this.robotsTxtCleanupThread = new RobotsTxtCleanupThread(new File(DB_PATH)); 
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		
		// register the protocol filter as service
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {"org.paxle.crawler.in","org.paxle.parser.out; pos=70;"});
		bc.registerService(IFilter.class.getName(), new RobotsTxtFilter(robotsTxtManager), filterProps);		
		
		// register robots.txt manager as service
		Hashtable<String, String[]> managerProps = new Hashtable<String, String[]>();
		bc.registerService(IRobotsTxtManager.class.getName(), this.robotsTxtManager, managerProps);	
		
		/*
		 * Create configuration if not available
		 */
		ServiceReference cmRef = bc.getServiceReference(ConfigurationAdmin.class.getName());
		if (cmRef != null) {
			ConfigurationAdmin cm = (ConfigurationAdmin) bc.getService(cmRef);
			Configuration config = cm.getConfiguration(IRobotsTxtManager.class.getName());
			if (config.getProperties() == null) {
				config.update(this.robotsTxtManager.getDefaults());
			}
			config = cm.getConfiguration(RobotsTxtCleanupThread.class.getName());
			if (config.getProperties() == null) {
				config.update(this.robotsTxtCleanupThread.getDefaults());
			}
		}
		
		/* 
		 * Register as managed service
		 * 
		 * ATTENTION: it's important to specify a unique PID, otherwise
		 * the CM-Admin service does not recognize us.
		 */
		Hashtable<String,Object> msProps = new Hashtable<String, Object>();
		msProps.put(Constants.SERVICE_PID, IRobotsTxtManager.class.getName());
		bc.registerService(ManagedService.class.getName(), this.robotsTxtManager, msProps);		
		
		Hashtable<String,Object> msP = new Hashtable<String, Object>();
		msP.put(Constants.SERVICE_PID, RobotsTxtCleanupThread.class.getName());
		bc.registerService(ManagedService.class.getName(), this.robotsTxtCleanupThread, msP);
		this.robotsTxtCleanupThread.start(); //we start it here, as the config is not available earlier
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		this.robotsTxtManager.terminate();
		this.robotsTxtManager = null;
		this.robotsTxtCleanupThread.interrupt();
		this.robotsTxtCleanupThread = null;
	}
}