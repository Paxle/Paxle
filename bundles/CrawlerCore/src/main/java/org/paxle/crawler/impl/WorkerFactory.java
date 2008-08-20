package org.paxle.crawler.impl;

import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;
import org.paxle.crawler.ISubCrawlerManager;

public class WorkerFactory implements IWorkerFactory<CrawlerWorker> {
	
	private final ISubCrawlerManager subCrawlerManager;

	/**
	 * @param subCrawlerManager a reference to the {@link SubCrawlerManager subcrawler-manager} which should
	 * be passed to a newly created {@link CrawlerWorker}.
	 */
	public WorkerFactory(ISubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
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
		// nothing todo here
	}
}
