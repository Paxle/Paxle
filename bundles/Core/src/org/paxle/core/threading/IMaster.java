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
}
