/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.data.balancer.impl;

import java.net.URI;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.paxle.core.queue.ICommand;

public class Balancer {
	
	private final ReentrantLock lock = new ReentrantLock();
	private final Condition notFull = lock.newCondition();
	private final DelayQueue<QueueHostConfig<ICommand>> queue = new DelayQueue<QueueHostConfig<ICommand>>();
	private final Map<String,QueueHostConfig<ICommand>> qmap = new WeakHashMap<String,QueueHostConfig<ICommand>>();
	
	private final int rejectThreshold;
	private final int maxCount;
	private final HostManager manager;
	
	private int count = 0;
	
	public Balancer(final int maxCount, final int rejectThreshold, final HostManager manager) {
		this.maxCount = maxCount;
		this.rejectThreshold = rejectThreshold;
		this.manager = manager;
	}
	
	public int size() {
		lock.lock();
		try {
			return count;
		} finally { lock.unlock(); }
	}
	
	public int size(final String auth) {
		lock.lock();
		try {
			final QueueHostConfig<?> config = qmap.get(auth);
			return (config == null) ? -1 : config.getQueue().size();
		} finally { lock.unlock(); }
	}
	
	public ICommand take() throws InterruptedException {
		ICommand e = null;
		lock.lock();
		try {
			while (e == null) {
				final QueueHostConfig<ICommand> c = queue.take();
				final BlockingQueue<ICommand> q = c.getQueue();
				e = q.poll();
				if (!q.isEmpty()) {
					c.reset();
					queue.put(c);
				}
			}
			count--;
			notFull.signal();
		} finally { lock.unlock(); }
		return e;
	}
	
	private BlockingQueue<ICommand> getQueue(final URI uri) {
		QueueHostConfig<ICommand> config = qmap.get(uri.getAuthority());
		if (config != null)
			return config.getQueue();
		final BlockingQueue<ICommand> q = new LinkedBlockingQueue<ICommand>();
		config = new QueueHostConfig<ICommand>(manager.getHostConfig(uri), q);
		qmap.put(uri.getAuthority(), config);
		queue.put(config);
		return q;
	}
	
	public boolean offer(final ICommand entry) {
		lock.lock();
		try {
			if (count >= maxCount)
				return false;
			final URI uri = entry.getLocation();
			final BlockingQueue<ICommand> q = getQueue(uri);
			if (q.size() < rejectThreshold) {
				final boolean r;
				if (r = q.offer(entry))
					count++;
				return r;
			} else {
				return false;
			}
		} finally { lock.unlock(); }
	}
	
	public void put(final ICommand entry) throws InterruptedException {
		lock.lock();
		try {
			while (count >= maxCount)
				notFull.await();
			final URI uri = entry.getLocation();
			final BlockingQueue<ICommand> q = getQueue(uri);
			q.put(entry);
			count++;
		} finally { lock.unlock(); }
	}
	
	@Override
	public String toString() {
		return queue.toString();
	}
}
