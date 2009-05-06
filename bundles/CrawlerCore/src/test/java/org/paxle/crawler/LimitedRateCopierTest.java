/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.crawler;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.output.NullOutputStream;
import org.paxle.crawler.CrawlerTools.LimitedRateCopier;

public class LimitedRateCopierTest extends TestCase {

	public void testThreadedCopy() throws Exception {
		// FIXME if size is in the order of magnitude of limitKBps, this test works, but ...
		final int size = 1024 * 4;		// ... try to increase this value ...
		final int threadNum = 4;
		final int limitKBps = 8;		// ... and this one proportionally, and watch how it explodes :)
		
		final int expectedTime = size / 1024 * 1000 / limitKBps * threadNum;
		final LimitedRateCopier lrc = new LimitedRateCopier(limitKBps); 
		// System.out.println("expected time: " + expectedTime + " ms");
		
		final ArrayList<Thread> threads = new ArrayList<Thread>();
		final Object sync = new Object();
		for (int i=0; i<threadNum; i++) {
			final int num = i;
			final InputStream zis = new NullInputStream(size);
			final OutputStream nos = new NullOutputStream();
			threads.add(new Thread() {
				{ this.setName("Test-thread " + num); }
				@Override
				public void run() {
					try {
					//	System.out.println("thread " + num + " syncing");
						synchronized (sync) { sync.wait(); }
					//	System.out.println("thread " + num + " starts copying");
						
						final long start = System.currentTimeMillis();
						lrc.copy(zis, nos, size);
						final long end = System.currentTimeMillis();
						/* XXX this is wrong, because every new thread gets less bandwidth One would have to pre-set the
						 * number of expected threads in the lrc for this to be correct
						assertTrue(
								String.format("%d: Threaded copying took %d ms but should have taken %d ms.", num, end - start, expectedTime),
								expectedTime <= end - start);
						 */
					//	System.out.println("thread " + num + " finished in " + (end - start) + " ms");
					} catch (Throwable e) { e.printStackTrace(); }
				}
			});
		}
		
		for (final Thread t : threads)
			t.start();
		Thread.sleep(10);		// wait until all have started and are waiting on sync
		
		// System.out.println("notifying all");
		
		final long start = System.currentTimeMillis();
		synchronized (sync) { sync.notifyAll(); }
		for (final Thread t : threads)
			t.join();
		final long end = System.currentTimeMillis();

		// System.out.println(String.format("Finished in %d ms", end - start));
		assertTrue(String.format("All %d threads took %d ms but should have taken %d ms.", threadNum, end - start, expectedTime),
				expectedTime <= end - start);
	}
	
	public void testIsUnsupportedCharset() {
		assertFalse(CrawlerTools.isUnsupportedCharset("UTF-8"));
	}
}
