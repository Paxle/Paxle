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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.filter.robots.impl.rules.RobotsTxt;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.config.QueryConfiguration;
import com.db4o.config.QueryEvaluationMode;
import com.db4o.events.Event4;
import com.db4o.events.EventArgs;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.events.ObjectEventArgs;
import com.db4o.ext.DatabaseClosedException;
import com.db4o.ext.ExtObjectContainer;
import com.db4o.ext.StoredClass;
import com.db4o.io.CachedIoAdapter;
import com.db4o.io.NonFlushingIoAdapter;
import com.db4o.io.RandomAccessFileAdapter;
import com.db4o.osgi.Db4oService;
import com.db4o.query.Predicate;
import com.db4o.query.Query;

public class Db4oStore implements IRuleStore {
	public static final String NAME = "robotsDB.db4o";
	
	/**
	 * A simple counter to avoid querying the database for size
	 */
	private int size = -1;
	private Object sizeSync = new Object();
	
	/**
	 * Path where {@link RobotsTxt} objects should be stored
	 */
	private final File path;
	
	/**
	 * The DB-file
	 */
	private final File dbFile;
	
	/**
	 * The DB configuration
	 */
	private final Configuration config;
	
	/**
	 * OSGi service for DB creation
	 */
	private final Db4oService dboService;
	
	/**
	 * Object DB
	 */
	private final ObjectContainer db;
	
	/**
	 * A task to remove outdated {@link RobotsTxt} from the {@link #db}
	 */
	private DBCleanupTask dbCleanupTask;
	
	/**
	 * {@link Timer} for the {@link #dbCleanupTask}
	 */
	private Timer dbCleanupTimer;
	
	/**
	 * For Logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	public Db4oStore(Db4oService dboService, File path) {
		this(dboService, path, 0);
	}
	
	/**
	 * 
	 * @param dboService
	 * @param path
	 * @param cleanupDelayMs delay in milliseconds before the db-cleanup-task is to be executed.
	 */
	Db4oStore(Db4oService dboService, File path, long cleanupDelayMs) {
		this.dboService = dboService;
		
		// create directory if required
		this.path = path;
		if (!this.path.exists()) this.path.mkdirs();
		
		// db file
		this.dbFile = new File(this.path,NAME);

		// configure to use indexes
		this.config = (this.dboService == null) 
				? Db4o.newConfiguration()
				: this.dboService.newConfiguration();
		this.config.objectClass(RobotsTxt.class).objectField("hostPort").indexed(true);		
		this.config.callbacks(false);
		this.config.blockSize(8);
		this.config.io(new CachedIoAdapter(new NonFlushingIoAdapter(new RandomAccessFileAdapter())));
		
		// create DB
		this.db = (this.dboService == null) 
				? Db4o.openFile(config, dbFile.toString())
				: this.dboService.openFile(config, dbFile.toString());
		
		// getting initial DB size
		this.size = this.size();
		this.logger.info(String.format(
				"Robots.txt DB contains %d entries.",
				this.size
		));
		
		// register event-listeners
		EventRegistry registry = EventRegistryFactory.forObjectContainer(this.db);  
		registry.created().addListener(new EventListener4(){
			public void onEvent(Event4 e, EventArgs args) {
				if (args instanceof ObjectEventArgs) {
					Object obj = ((ObjectEventArgs)args).object();
					if (obj instanceof RobotsTxt) {
						onCreated((RobotsTxt)obj);
					}
				}
			}}
		);
		registry.deleted().addListener(new EventListener4(){
			public void onEvent(Event4 e, EventArgs args) {
				if (args instanceof ObjectEventArgs) {
					Object obj = ((ObjectEventArgs)args).object();
					if (obj instanceof RobotsTxt) {
						onDeleted((RobotsTxt)obj);
					}
				}
			}}
		);		
		
		// robots.txt cleanup can be skipped via variable
		boolean skipCleanup = Boolean.parseBoolean(System.getProperty("robots.Db4oStore.skipCleanup","false"));
		if (!skipCleanup) {
			// create and init cleanup task
			this.dbCleanupTask = new DBCleanupTask();
			this.dbCleanupTimer = new Timer("Robots.txt Cleanup Timer");
			this.dbCleanupTimer.scheduleAtFixedRate(this.dbCleanupTask, cleanupDelayMs, 30*60*1000);
		}
	}
	
	private void onCreated(RobotsTxt robots) {
		synchronized (sizeSync) {
			size++;
		}		
		
		if (logger.isTraceEnabled()) {
			logger.trace(String.format(
					"Robots.txt created:\n%s",
					robots.toString()
			));
		}	
	}
	
	private void onDeleted(RobotsTxt robots) {
		synchronized (sizeSync) {
			size--;
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace(String.format(
					"Robots.txt deleted:\n%s",
					robots.toString()
			));
		}		
	}
	
	public int size() {
		if (this.size >= 0) return this.size;		
		final StoredClass sc = db.ext().storedClass(RobotsTxt.class);
		return (sc==null) ? 0 : sc.instanceCount();
	}
	
	public RobotsTxt read(String hostPort) throws IOException {
		Query query = db.query();
		query.constrain(RobotsTxt.class);
		query.descend("hostPort").constrain(hostPort);
		ObjectSet<RobotsTxt> result = query.execute();
		return result.hasNext()
				? result.next()
				: null;
	}

	public void write(RobotsTxt robotsTxt) throws IOException {
		db.store(robotsTxt);
		db.commit();		
	}

	public void close() throws IOException {
		if (this.dbCleanupTimer != null) this.dbCleanupTimer.cancel();
		if (this.dbCleanupTask != null) this.dbCleanupTask.cancel();
		db.close();
	}
	
	/**
	 * Function to manually run the {@link DBCleanupTask}.
	 * This function should only be called for testing purpose.
	 */
	void runCleanup() {
		this.dbCleanupTask.run();
	}
	
	/**
	 * A task to remove outdated {@link RobotsTxt} from the {@link #db}
	 */
	class DBCleanupTask extends TimerTask {
		private Log logger = LogFactory.getLog(this.getClass());
		private boolean canceled = false;
		
		@Override
		public boolean cancel() {
			this.canceled = true;
			return super.cancel();
		}
		
		@Override
		public void run() {
			final int oldPriority = Thread.currentThread().getPriority();
			try {
				Thread.currentThread().setPriority(3);
				this.logger.info("Starting robots.txt DB cleanup.");
				this.canceled = false;
				final Date now = new Date();
				
				// query for outdated robots.txt entries
				long start = System.currentTimeMillis();
				List <RobotsTxt> outdatedRobotsTxts;
				final Predicate<RobotsTxt> predicate = new Predicate<RobotsTxt>() {
					private static final long serialVersionUID = 1L;
					
					@Override
					public boolean match(RobotsTxt robotsTxt) {
						return robotsTxt.getExpirationDate().before(now);
					}
				};
				
				final ExtObjectContainer ext = db.ext();
				final QueryConfiguration queryConfig = ext.configure().queries();
				synchronized (ext.lock()) {
					queryConfig.evaluationMode(QueryEvaluationMode.LAZY);
					try {
						outdatedRobotsTxts = ext.query(predicate);
					} finally { queryConfig.evaluationMode(QueryEvaluationMode.IMMEDIATE); }
				}
				
				final Iterator<RobotsTxt> it;
				
				if (outdatedRobotsTxts == null || !(it = outdatedRobotsTxts.iterator()).hasNext()) {
					this.logger.debug("No outdated robots.txt entries found in DB.");
					return;
				} else {
					this.logger.debug(String.format(
							"Querying of outdated robots.txt entries took %d ms.",
							Long.valueOf(System.currentTimeMillis() - start)
					));
				}
				
				// loop through the list and delete all entries
				int c = 0;
				while (!this.canceled && it.hasNext()) {
					db.delete(it.next());
					c++;
				}
				db.commit();
				
				this.logger.info(String.format(
						"Robots.txt DB cleanup finished. %d entries removed.",
						Integer.valueOf(c)
				));
			} catch (Exception e) {
				if (e instanceof DatabaseClosedException && this.canceled) {
					this.logger.info("Shutdown of DB during cleanup detected.");
				} else {				
					this.logger.error(String.format(
							"Unexpected '%s' while trying to delete outdated robots.txt entries from DB",
							e.getClass().getName()
					),e);
				}
			} finally { Thread.currentThread().setPriority(oldPriority); }
		}
	}
}
