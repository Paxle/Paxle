package org.paxle.parser.impl;

import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<IWorker> {
	
	private SubParserManager subParserManager = null;
	
	public WorkerFactory(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}
	
	/**
	 * Creates a new {@link ParserWorker} by order of the
	 * worker-pool
	 */
	public IWorker makeObject() throws Exception {
		return new ParserWorker(this.subParserManager);
	}
}
