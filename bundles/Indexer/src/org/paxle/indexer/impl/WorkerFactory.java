package org.paxle.indexer.impl;

import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<IWorker> {
	
	public IWorker makeObject() throws Exception {
		return new IndexerWorker();
	}
}
