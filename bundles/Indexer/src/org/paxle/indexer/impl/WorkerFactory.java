package org.paxle.indexer.impl;

import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<IWorker> {
	
	public IWorker createWorker() throws Exception {
		return new IndexerWorker();
	}

	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#initWorker(IWorker)
	 */		
	public void initWorker(IWorker worker) {
		// TODO Auto-generated method stub
		
	}
}
