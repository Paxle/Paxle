
package org.paxle.data.balancer.impl;

import java.util.concurrent.TimeUnit;

import org.paxle.data.balancer.IHostConfig;

public class HostConfig implements IHostConfig {
	
	private final long delayMs;
	
	public HostConfig(final long delay, final TimeUnit unit) {
		this.delayMs = unit.toMillis(delay);
	}
	
	public long getDelayMs() {
		return delayMs;
	}
}