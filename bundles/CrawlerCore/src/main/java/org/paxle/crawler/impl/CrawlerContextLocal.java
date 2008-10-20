/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.crawler.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	
	private Set<String> supportedMimeTypes = Collections.synchronizedSet(new HashSet<String>());
	
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
	
	public Set<String> getSupportedMimeTypes() {
		return this.supportedMimeTypes;
	}
	
}
