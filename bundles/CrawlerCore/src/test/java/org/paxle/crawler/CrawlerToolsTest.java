
package org.paxle.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.paxle.crawler.CrawlerTools.LimitedRateCopier;

import junit.framework.TestCase;

public class CrawlerToolsTest extends TestCase {
	
	private static class ZeroInputStream extends InputStream {
		@Override
		public int read() throws IOException {
			return 0;
		}
	}
	
	private static class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
		}
	}
	
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
			final InputStream zis = new ZeroInputStream();
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
}
