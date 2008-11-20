/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.filter.robots.impl;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.monitor.Monitorable;
import org.paxle.core.filter.IFilter;
import org.paxle.filter.robots.IRobotsTxtManager;
import org.paxle.filter.robots.impl.store.Db4oStore;
import org.paxle.filter.robots.impl.store.EhCacheDiskStore;
import org.paxle.filter.robots.impl.store.FileStore;
import org.paxle.filter.robots.impl.store.IRuleStore;

import com.db4o.osgi.Db4oService;

public class Activator implements BundleActivator {
	private static String DB_PATH = "robots-db";
	private static String DB_TYPE = "ehCache";
	
	private RobotsTxtManager robotsTxtManager = null;
	private RobotsTxtCleanupThread robotsTxtCleanupThread = null;
	private IRuleStore ruleStore = null;
	
	/**
	 * This function is called by the osgi-framework to start the bundle.
	 * @see BundleActivator#start(BundleContext) 
	 */		
	public void start(BundleContext bc) throws Exception {
		final String dataPath = System.getProperty("paxle.data") + File.separatorChar + DB_PATH;
		
		// testing if the DB4o Service is available
		ServiceReference db4oServiceRef = bc.getServiceReference("com.db4o.osgi.Db4oService");
		if (db4oServiceRef != null && DB_TYPE.equalsIgnoreCase("db4o")) {
			// using DB4O
			Db4oService dboService = (Db4oService) bc.getService(db4oServiceRef);			
			this.ruleStore = new Db4oStore(dboService, new File(dataPath));
		} else if (DB_TYPE.equalsIgnoreCase("file")) {
			// using a file-store
			this.ruleStore = new FileStore(new File(DB_PATH));
			
			// init a cleanup thread
			this.robotsTxtCleanupThread = new RobotsTxtCleanupThread(new File(dataPath)); 
		} else {
			@SuppressWarnings("unchecked")
			Enumeration<URL> configFileEnum = bc.getBundle().findEntries("/resources/", "ehCache.xml", true);
			URL configFile = (configFileEnum.hasMoreElements()) ? configFileEnum.nextElement() : null;
			this.ruleStore = new EhCacheDiskStore(dataPath, configFile);
		}
		
		// init the robots.txt-manager
		this.robotsTxtManager = new RobotsTxtManager(this.ruleStore);
		
		/* ==========================================================
		 * Register Services provided by this bundle
		 * ========================================================== */		
		
		// register the protocol filter as service
		Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
		filterProps.put(IFilter.PROP_FILTER_TARGET, new String[] {
				// apply filter to the crawler-input-queue
				"org.paxle.crawler.in",
				// apply filter to the parser-output-queue at pos 70
				String.format("org.paxle.parser.out; %s=%d", IFilter.PROP_FILTER_TARGET_POSITION,Integer.valueOf(70))
		});
		bc.registerService(IFilter.class.getName(), new RobotsTxtFilter(robotsTxtManager), filterProps);		
		
		// register robots.txt manager as service
		Hashtable<String,Object> managerProps = new Hashtable<String,Object>();
		managerProps.put(Constants.SERVICE_PID, RobotsTxtManager.MONITOR_PID);
		bc.registerService(new String[] {
				IRobotsTxtManager.class.getName(),
				Monitorable.class.getName()
		}, this.robotsTxtManager, managerProps);	
		
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
			
			if (this.robotsTxtCleanupThread != null) {
				config = cm.getConfiguration(RobotsTxtCleanupThread.class.getName());
				if (config.getProperties() == null) {
					config.update(this.robotsTxtCleanupThread.getDefaults());
				}
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
		
		if (this.robotsTxtCleanupThread != null) {
			Hashtable<String,Object> msP = new Hashtable<String, Object>();
			msP.put(Constants.SERVICE_PID, RobotsTxtCleanupThread.class.getName());
			bc.registerService(ManagedService.class.getName(), this.robotsTxtCleanupThread, msP);
			this.robotsTxtCleanupThread.start(); //we start it here, as the config is not available earlier
		}
	}

	/**
	 * This function is called by the osgi-framework to stop the bundle.
	 * @see BundleActivator#stop(BundleContext)
	 */	
	public void stop(BundleContext context) throws Exception {
		// terminate robots.txt  manager
		this.robotsTxtManager.terminate();
		this.robotsTxtManager = null;
		
		// close store
		this.ruleStore.close();
		
		// terminate cleanup-thread
		if (this.robotsTxtCleanupThread != null) {
			this.robotsTxtCleanupThread.interrupt();
			this.robotsTxtCleanupThread = null;
		}
	}
}