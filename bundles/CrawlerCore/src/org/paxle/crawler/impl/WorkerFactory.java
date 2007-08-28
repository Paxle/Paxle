package org.paxle.crawler.impl;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.crypt.md5.IMD5;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<CrawlerWorker> {
	
	private SubCrawlerManager subCrawlerManager = null;
	private ICharsetDetector charsetDetector = null;
	private IMD5 md5 = null;
	
	/**
	 * @param subCrawlerManager a reference to the {@link SubCrawlerManager subcrawler-manager} which should
	 * be passed to a newly created {@link CrawlerWorker}.
	 */
	public WorkerFactory(SubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
	}
	
	/**
	 * @param charsetDetector the {@link ICharsetDetector} that should be passed 
	 *        to the {@link ParserWorker worker-thread} on {@link #initWorker(CrawlerWorker) initialization}.
	 */
	public void setCharsetDetector(ICharsetDetector charsetDetector) {
		this.charsetDetector = charsetDetector;
	}
	
	public void setMD5(IMD5 md5) {
		this.md5 = md5;
	}
	
	/**
	 * Creates a new {@link CrawlerWorker} by order of the worker-pool
	 */
	public CrawlerWorker createWorker() throws Exception {
		return new CrawlerWorker(subCrawlerManager);
	}

	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#initWorker(IWorker)
	 */
	public void initWorker(CrawlerWorker worker) {
		worker.charsetDetector = this.charsetDetector;
		worker.md5 = this.md5;
	}
}
