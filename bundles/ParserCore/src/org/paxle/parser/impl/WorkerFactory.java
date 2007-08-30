package org.paxle.parser.impl;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<ParserWorker> {
	
	private SubParserManager subParserManager = null;
	private IMimeTypeDetector mimeTypeDetector = null; 
	private ICharsetDetector charsetDetector = null;
	private ITempFileManager tempFileManager = null;
	
	/**
	 * @param subParserManager the {@link SubParserManager} that should be passed 
	 *        to the {@link ParserWorker worker-thread} on {@link #createWorker() worker-creation}
	 */
	public WorkerFactory(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;		
	}
	
	/**
	 * @param charsetDetector the {@link IMimeTypeDetector} that should be passed 
	 *        to the {@link ParserWorker worker-thread} on {@link #initWorker(ParserWorker) initialization}.
	 */	
	public void setMimeTypeDetector(IMimeTypeDetector mimeTypeDetector) {
		this.mimeTypeDetector = mimeTypeDetector;
	}
	
	/**
	 * @param charsetDetector the {@link ICharsetDetector} that should be passed 
	 *        to the {@link ParserWorker worker-thread} on {@link #initWorker(ParserWorker) initialization}.
	 */
	public void setCharsetDetector(ICharsetDetector charsetDetector) {
		this.charsetDetector = charsetDetector;
	}
	
	public void setTempFileManager(ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
	}
	
	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#createWorker()
	 */
	public ParserWorker createWorker() throws Exception {
		return new ParserWorker(this.subParserManager);
	}

	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#initWorker(IWorker)
	 */		
	public void initWorker(ParserWorker worker) {
		worker.mimeTypeDetector = this.mimeTypeDetector;
		worker.charsetDetector = this.charsetDetector;
		worker.tempFileManager = this.tempFileManager;
	}
}
