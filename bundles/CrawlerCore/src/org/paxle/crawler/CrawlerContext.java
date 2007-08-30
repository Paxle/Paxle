package org.paxle.crawler;

import java.util.HashMap;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.crypt.md5.IMD5;
import org.paxle.core.io.temp.ITempFileManager;

public class CrawlerContext {
    private static final ThreadLocal<CrawlerContext> context = new ThreadLocal<CrawlerContext>();
	
    private ICharsetDetector charsetDetector = null;
    private ITempFileManager tempFileManager = null;
    private HashMap<String, Object> bag = new HashMap<String, Object>();
    private final IMD5 md5;
    
    public CrawlerContext(ICharsetDetector charsetDetector, IMD5 md5, ITempFileManager tempFileManager) {
    	this.charsetDetector = charsetDetector;
    	this.md5 = md5;
    	this.tempFileManager = tempFileManager;
	}

	public static void setCurrentContext(CrawlerContext parserContext) {
    	context.set(parserContext);
    }    
    
	public static CrawlerContext getCurrentContext() {
		return context.get();		
	}	
	
	/**
	 * @return a class that can be used to detect the charset of a resource
	 * This reference may be <code>null</code> if no {@link ICharsetDetector charset-detector} is available.
	 */
	public ICharsetDetector getCharsetDetector() {
		return this.charsetDetector;
	}
	
	public IMD5 getMD5() {
		return this.md5;
	}
	
	public ITempFileManager getTempFileManager() {
		return this.tempFileManager;
	}
	
	/* ========================================================================
	 * Function operating on the property bag
	 * ======================================================================== */	
	
	public Object getProperty(String name) {
		return this.bag.get(name);
	}
	
	public void setProperty(String name, Object value) {
		this.bag.put(name, value);
	}
	
	public void removeProperty(String name) {		
		this.bag.remove(name);
	}
	
	public void reset() {
		this.bag.clear();
	}
}
