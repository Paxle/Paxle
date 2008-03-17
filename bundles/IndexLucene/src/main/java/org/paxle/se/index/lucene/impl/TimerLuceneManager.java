
package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class TimerLuceneManager extends AFlushableLuceneManager {
	
	private final Timer timer = new Timer("LuceneFlushTimer");
	private final TimerTask flushTTask = new FlushTTask();
	
	private class FlushTTask extends TimerTask {
		@Override
		public void run() {
			try { TimerLuceneManager.this.checkFlush(); } catch (IOException e) { e.printStackTrace(); }
		}
	}
	
	public TimerLuceneManager(String path, int flushDelay, int flushPeriod) throws IOException {
		super(path);
		this.timer.schedule(this.flushTTask, flushDelay, flushPeriod);
	}
	
	@Override
	public void close() throws IOException {
		this.timer.cancel();
		super.close();
	}
}
