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
package org.paxle.filter.robots.impl.store;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;
import org.paxle.filter.robots.impl.rules.RobotsTxt;

public class FileStoreCleanupThread extends Thread implements ManagedService
{ 
	/** the directory where the robots.txt data is stored */
	File dir = null;
	
	private static final String PID = FileStoreCleanupThread.class.getName();
	
	/** The time in minutes between each cleaning */
	public static final String PROP_CLEANDELAY = PID + '.' + "cleandelay";

	/** The delay between each read-action on the stored robots.txt objects in seconds */
	public static final String PROP_IODELAY = PID + '.' + "iodelay";

	/** The configuration data for this class */
	private Dictionary<String, Object> config = null;

	/** The start of the last run */
	long tstart;

	/** The logging facility */
	Log logger = LogFactory.getLog(this.getClass());

	/**
	 * Generates a thread which wipes out robots.txt entries from the cache, which are older than their ExpirationDate
	 * @param dir the directory where the robots.txt objects are stored
	 */
	FileStoreCleanupThread(File dir) 
	{
		this.dir = dir;
		this.setName("RobotsTxtCleanupThread");
		this.setPriority(Thread.MIN_PRIORITY);
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		/* If set to false the thread terminates */
		boolean go_on = true;

		while (go_on) {

			/* counter for number of files */
			int ctr = 0;

			/* counter for number of deleted files */
			int delctr = 0;

			/* timestamp of start */
			tstart = System.currentTimeMillis();

			logger.info("Cleaning cache...");

			for ( File file : dir.listFiles() )  {
				if (!file.isFile()) {
					logger.warn("The cache should only contain files. Please review non-file element " + file.toURI() + " manually.");
				} else {
					ObjectInputStream ois = null;
					RobotsTxt robotsTxt = null;
					ctr++;
					try {
						ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
						robotsTxt = (RobotsTxt) ois.readObject();
						ois.close();
						ois = null;
					} catch (Exception e) {
						if (e instanceof InvalidClassException) {
							//Old type of class definition. Just delete it to avoid cruft.
							file.delete();
						} else if (e instanceof ClassNotFoundException) {
							// class was refactored. Just delete it to avoid cruft.
							file.delete();
						} else {
							logger.error("Error while reading file " + file.getName() + " - Removing it.", e);
							file.delete();
						}
						delctr++;
					} finally {
						if (ois != null) try { ois.close(); } catch (Exception e) {/* ignore this */}
					}

					if (robotsTxt != null && (robotsTxt.getExpirationDate().getTime() < System.currentTimeMillis())) {
						logger.debug("Deleting cached robots.txt file for " + robotsTxt.getHostPort());
						file.delete();
						delctr++;
					}
				}
				if (isInterrupted()) {
					go_on=false;
					break;
				}
				
				checkConfig();
				try {
					//Wait a moment to not overload the I/O
					synchronized(this) { this.wait(((Integer)config.get(PROP_IODELAY)).intValue()); }
				} catch ( InterruptedException e ) {
					go_on=false;
					break;
				}
			}

			logger.debug("Finished cleaning cache.\n" +
					"Total Runtime: " + ((System.currentTimeMillis() - tstart)/1000) + "s\n" +
					"I/O-delay is configured to " + config.get(PROP_IODELAY) + "ms\n" + 
					"I/O-delay accounted for " + (ctr * (Integer)config.get(PROP_IODELAY) / 1000) + "s of total time\n" +
					"Processing time: " + (((System.currentTimeMillis() - tstart)/1000) - (ctr * (Integer)config.get(PROP_IODELAY) / 1000)) + "\n" +
					"Files before: " + ctr + "\n" +
					"Files deleted: " + delctr + "\n" +
					"Files now: " + (ctr - delctr));

			if (go_on) {
				checkConfig();
				try {
					synchronized(this) { this.wait(((Integer)config.get(PROP_CLEANDELAY)).intValue()*60*1000); }
				} catch ( InterruptedException e ) {
					go_on=false;
				}
			}
		}
		logger.info("Thread terminated.");
	}

	/**
	 * Checks the configuration of the CleanupThread for completeness
	 */
	private void checkConfig() {
		if (config.get(PROP_CLEANDELAY) == null) {
			logger.warn("Delay for robots.txt cleaning is null. Loading defaults.");
			updated(getDefaults());
		}
		if (config.get(PROP_IODELAY) == null) {
			logger.warn("IO-Delay for robots.txt cleaning is null. Loading defaults.");
			updated(getDefaults());
		}
	}

	/**
	 * @see ManagedService#updated(Dictionary)
	 */
	@SuppressWarnings("unchecked")		// we're only implementing an interface
	public synchronized void updated(Dictionary configuration) {
		logger.info("Updating configuration");
		try {
			if ( configuration == null ) {
				logger.warn("Updated configuration is null. Using defaults.");
				configuration = this.getDefaults();
			}			
			this.config = configuration;
		} catch (Throwable e) {
			logger.error("Internal exception during configuring", e);
		}

		/*
		 * Most probably the thread already slept some time.
		 * If this time is greater than the newly configured time, start execution immediately
		 */

		if ((System.currentTimeMillis() - tstart) > (((Integer)config.get(PROP_CLEANDELAY)).longValue()*60l*1000l)) {
			logger.debug("Newly configured run intervall is smaller than actual waiting time. Starting new run immediately.");
			synchronized(this) { this.notify(); }
		}

	}

	/**
	 * @return the default configuration of this service
	 */
	public Hashtable<String,Object> getDefaults() {
		Hashtable<String,Object> defaults = new Hashtable<String,Object>();

		defaults.put(PROP_CLEANDELAY, Integer.valueOf(30));
		defaults.put(PROP_IODELAY, Integer.valueOf(500));
		defaults.put(Constants.SERVICE_PID, PID);

		return defaults;
	}

}
