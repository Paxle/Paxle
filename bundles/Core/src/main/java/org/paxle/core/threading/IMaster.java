package org.paxle.core.threading;

public interface IMaster {
	/**
	 * TODO: do we need an explicit start?
	 */
	public void start();
	
	public void terminate();
	
	public void pauseMaster();
	
	public void resumeMaster();
	
	public boolean isPaused();
	
	/**
	 * @return the PPM of this component since startup
	 */
	public int getPPM();
	
	/**
	 * Process the next job in the queue if the componend was paused
	 */
	public void processNext();	
}
