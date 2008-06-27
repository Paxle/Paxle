package org.paxle.filter.robots.impl.store;

import java.io.File;
import java.io.IOException;

import org.paxle.filter.robots.impl.rules.RobotsTxt;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.Configuration;
import com.db4o.osgi.Db4oService;
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
	
	private final Db4oService dboService;
	
	private final ObjectContainer db;
	
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
		
		this.db = (this.dboService == null) 
				? Db4o.openFile(config, dbFile.toString())
				: this.dboService.openFile(config, dbFile.toString());
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
		db.close();
	}
}