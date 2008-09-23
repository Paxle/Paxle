package org.paxle.filter.robots.impl.store;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
import com.db4o.ext.DatabaseClosedException;
import com.db4o.osgi.Db4oService;
import com.db4o.query.Predicate;
import com.db4o.query.Query;

public class Db4oStore implements IRuleStore {
	public static final String NAME = "robotsDB.db4o";
	
	
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
	
	public Db4oStore(Db4oService dboService, File path) {
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
		
		this.db = (this.dboService == null) 
				? Db4o.openFile(config, dbFile.toString())
				: this.dboService.openFile(config, dbFile.toString());
				
		// robots.txt cleanup can be skipped via variable
		Boolean skipCleanup = Boolean.valueOf(System.getProperty("robots.Db4oStore.skipCleanup","false"));
		if (!skipCleanup) {
			// create and init cleanup task
			this.dbCleanupTask = new DBCleanupTask();
			this.dbCleanupTimer = new Timer();
			this.dbCleanupTimer.scheduleAtFixedRate(this.dbCleanupTask, 0, 30*60*1000);
		}
	}

	@SuppressWarnings("unchecked")
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
			try {
				this.logger.info("Starting robots.txt DB cleanup.");
				this.canceled = false;
				final Date now = new Date();

				// query for outdated robots.txt entries
				long start = System.currentTimeMillis();
				List <RobotsTxt> outdatedRobotsTxts = db.query(new Predicate<RobotsTxt>() {
					private static final long serialVersionUID = 1L;

					public boolean match(RobotsTxt robotsTxt) {
						return robotsTxt.getExpirationDate().before(now);
					}
				});
				if (outdatedRobotsTxts == null || outdatedRobotsTxts.size() == 0) {
					this.logger.debug("No outdated robots.txt entries found in DB.");
					return;
				} else {
					this.logger.debug(String.format(
							"Querying of outdated robots.txt entries took %d ms.",
							(System.currentTimeMillis()-start)
					));
				}
				
				// loop through the list and delete all entries
				for (RobotsTxt outdatedRobotsTxt : outdatedRobotsTxts) {
					if (this.canceled) break;
					db.delete(outdatedRobotsTxt);
				}
				db.commit();
				
				this.logger.info(String.format(
						"Robots.txt DB cleanup finished. %d entries removed.",
						outdatedRobotsTxts.size()
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
			}
		}
		
	}
}