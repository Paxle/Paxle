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

package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TimerLuceneManager extends AFlushableLuceneManager {
	
	private final Timer timer = new Timer("LuceneFlushTimer");
	private final TimerTask flushTTask = new FlushTTask();
	private final Log logger = LogFactory.getLog(TimerLuceneManager.class);
	
	private class FlushTTask extends TimerTask {
		@Override
		public void run() {
			try {
				TimerLuceneManager.this.checkFlush();
			} catch (IOException e) {
				logger.error("I/O exception while flushing LuceneManager", e);
			}
		}
	}
	
	public TimerLuceneManager(String path, final PaxleAnalyzer analyzer, int flushDelay, int flushPeriod) throws IOException {
		super(path, analyzer);
		this.timer.schedule(this.flushTTask, flushDelay, flushPeriod);
	}
	
	@Override
	public void close() throws IOException {
		this.timer.cancel();
		super.close();
	}
}
