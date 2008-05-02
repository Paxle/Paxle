package org.paxle.gui.impl;

import org.apache.velocity.context.Context;
import org.osgi.framework.BundleException;
import org.paxle.gui.IServiceManager;

public class SysDownDelayThread extends Thread {
	
	boolean restart;
	int delay;
	Context context;
	IServiceManager sm;
	
	/**
	 * This whole thread is to be removed in the future. At the moment it is used to delay the shutdown of 
	 * the framework to show a last informational message.
	 * @param delay the delay of the shutdown in seconds
	 * @param restart restart the framework?
	 * @param sm an IServiceManager to shutdown/restart the framework
	 */
	SysDownDelayThread(int delay, boolean restart, IServiceManager sm) 
	{
		this.delay = delay;
		this.restart = restart;
		this.sm = sm;
		this.start();
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(this.delay*1000);
		} catch ( InterruptedException e ) {
		}
		
		if (restart) {
			try {
				this.sm.restartFramework();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.sm.shutdownFramework();
			} catch (BundleException e) {
				e.printStackTrace();
			}
		}
	}
}
