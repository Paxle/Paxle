package org.paxle.parser.impl;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

public class WorkerFactory implements IWorkerFactory<ParserWorker> {
	
	private final SubParserManager subParserManager;
	private ITempFileManager tempFileManager;
	private IReferenceNormalizer referenceNormalizer;
	private IMimeTypeDetector mimeTypeDetector = null; 
	private ICharsetDetector charsetDetector = null;
	
	/**
	 * @param subParserManager the {@link SubParserManager} that should be passed 
	 *        to the {@link ParserWorker worker-thread} on {@link #createWorker() worker-creation}
	 */
	public WorkerFactory(
			SubParserManager subParserManager,
			ITempFileManager tempFileManager,
			IReferenceNormalizer referenceNormalizer) {
		this.subParserManager = subParserManager;
		this.tempFileManager = tempFileManager;
		this.referenceNormalizer = referenceNormalizer;
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
	
	public void setReferenceNormalizer(final IReferenceNormalizer referenceNormalizer) {
		this.referenceNormalizer = referenceNormalizer;
	}
	
	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#createWorker()
	 */
	public ParserWorker createWorker() throws Exception {
	    ParserWorker parserWorker = new ParserWorker(this.subParserManager);
	    parserWorker.setPriority(2);
		return parserWorker;
	}

	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#initWorker(IWorker)
	 */		
	public void initWorker(ParserWorker worker) {
		worker.mimeTypeDetector = this.mimeTypeDetector;
		worker.charsetDetector = this.charsetDetector;
		worker.tempFileManager = this.tempFileManager;
		worker.referenceNormalizer = this.referenceNormalizer;
	}
}
