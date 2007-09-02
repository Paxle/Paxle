package org.paxle.crawler.impl;

import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.crawler.CrawlerContext;

public class CrawlerContextLocal extends ThreadLocal<CrawlerContext> {
	private IMimeTypeDetector mimeTypeDetector;
	private ICharsetDetector charsetDetector;
	private ICryptManager cryptManager;
	private ITempFileManager tempFileManager;
	
	public CrawlerContextLocal() {
		CrawlerContext.setThreadLocal(this);
	}
	
	@Override
	protected CrawlerContext initialValue() {
		return new CrawlerContext();
	}

	public void setCharsetDetector(ICharsetDetector detector) {
		this.charsetDetector = detector;
	}
	
	public ICharsetDetector getCharsetDetector() {
		return this.charsetDetector;
	}

	public void setCryptManager(ICryptManager manager) {
		this.cryptManager = manager;
	}
	
	public ICryptManager getCryptManager() {
		return this.cryptManager;
	}

	public void setMimeTypeDetector(IMimeTypeDetector detector) {
		this.mimeTypeDetector = detector;
	}
	
	public IMimeTypeDetector getMimeTypeDetector() {
		return this.mimeTypeDetector;
	}

	public ITempFileManager getTempFileManager() {
		return tempFileManager;
	}

	public void setTempFileManager(ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
	}
}
