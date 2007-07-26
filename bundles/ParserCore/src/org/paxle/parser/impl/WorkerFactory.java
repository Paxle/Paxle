package org.paxle.parser.impl;

import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<IWorker> {
	
	private SubParserManager subParserManager = null;
	private IMimeTypeDetector mimeTypeDetector = null; 
	
	public WorkerFactory(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;		
	}
	
	public void setMimeTypeDetector(IMimeTypeDetector mimeTypeDetector) {
		this.mimeTypeDetector = mimeTypeDetector;
	}
	
	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#createWorker()
	 */
	public IWorker createWorker() throws Exception {
		return new ParserWorker(this.subParserManager);
	}

	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#initWorker(IWorker)
	 */		
	public void initWorker(IWorker worker) {
		((ParserWorker)worker).mimeTypeDetector = this.mimeTypeDetector;
	}
}
