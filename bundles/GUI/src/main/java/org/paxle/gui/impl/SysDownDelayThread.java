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
