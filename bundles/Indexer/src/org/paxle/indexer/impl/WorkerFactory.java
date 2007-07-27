package org.paxle.indexer.impl;

import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<IndexerWorker> {
	
	public IndexerWorker createWorker() throws Exception {
		return new IndexerWorker();
	}

	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#initWorker(IWorker)
	 */		
	public void initWorker(IndexerWorker worker) {
		// TODO Auto-generated method stub
		
	}
}
