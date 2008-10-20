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

package org.paxle.data.balancer.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.paxle.data.balancer.IHostConfig;

public class QueueHostConfig<E> implements Delayed {
	
	private final AtomicLong delay = new AtomicLong();
	private final BlockingQueue<E> queue;
	private final IHostConfig config;
	
	public QueueHostConfig(final IHostConfig config, final BlockingQueue<E> queue) {
		this.config = config;
		this.queue = queue;
		delay.set(System.currentTimeMillis());
	}
	
	public int compareTo(Delayed o) {
		final long delta = getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS);
		return (delta < 0L) ? -1 : (delta > 0L) ? 1 : 0;
	}
	
	public long getDelay(TimeUnit unit) {
		return unit.convert(delay.get() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}
	
	public void reset() {
		delay.set(System.currentTimeMillis() + config.getDelayMs());
	}
	
	public BlockingQueue<E> getQueue() {
		return queue;
	}
	
	@Override
	public String toString() {
		return "delay: " + (delay.get() - System.currentTimeMillis()) + ", " + queue + "\n";
	}
}
