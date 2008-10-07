package org.paxle.data.db;

import java.net.URI;

public interface ICommandDB {
	
	public static final String PROP_URL_ENQUEUE_SINK = "org.paxle.data.command-db.sink";
	
	/**
	 * @return the total size of the command db
	 */
	public long size();
	
	/**
	 * @return the amount of commands that needs to be processed
	 */
	public long enqueuedSize();
	
	/**
	 * Function to test if a given {@link URI} is known to 
	 * the {@link ICommandDB command-db}
	 * @param location the given {@link URI}
	 * @return <code>true</code> if the location is known
	 */
	public boolean isKnown(URI location);
	
	/**
	 * Function to enqueue a {@link URI} as new {@link ICommand} into
	 * the {@link ICommandDB command-db}
	 * @param location the {@link URI} to enqueue
	 * @return <code>false</code> if the {@link URI} was not enqueued because it is already {@link #isKnown(URI) known}
	 */
	public boolean enqueue(URI location, int profileID, int depth);
}
