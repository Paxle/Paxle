package org.paxle.crawler.impl;

import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<CrawlerWorker> {
	
	private final SubCrawlerManager subCrawlerManager;
	private final ITempFileManager tempFileManager;
	private ICharsetDetector charsetDetector = null;
	private ICryptManager cryptManager;
	
	/**
	 * @param subCrawlerManager a reference to the {@link SubCrawlerManager subcrawler-manager} which should
	 * be passed to a newly created {@link CrawlerWorker}.
	 */
	public WorkerFactory(SubCrawlerManager subCrawlerManager, ITempFileManager tempFileManager) {
		this.subCrawlerManager = subCrawlerManager;
		this.tempFileManager = tempFileManager;
	}
	
	/**
	 * @param charsetDetector the {@link ICharsetDetector} that should be passed 
	 *        to the {@link ParserWorker worker-thread} on {@link #initWorker(CrawlerWorker) initialization}.
	 */
	public void setCharsetDetector(ICharsetDetector charsetDetector) {
		this.charsetDetector = charsetDetector;
	}
	
	public void setCryptManager(ICryptManager cryptManager) {
		this.cryptManager = cryptManager;
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
		worker.cryptManager = this.cryptManager;
		worker.tempFileManager = this.tempFileManager;
	}
}
