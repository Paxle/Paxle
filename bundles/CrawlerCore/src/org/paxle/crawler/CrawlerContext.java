package org.paxle.crawler;

import java.util.HashMap;

import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;

public class CrawlerContext {
    
	private static final ThreadLocal<CrawlerContext> context = new ThreadLocal<CrawlerContext>();

    private final HashMap<String, Object> bag = new HashMap<String, Object>();
    private final ICharsetDetector charsetDetector;
    private final ITempFileManager tempFileManager;
    private final ICryptManager cryptManager;
    
    public CrawlerContext(ICharsetDetector charsetDetector, ICryptManager cryptManager, ITempFileManager tempFileManager) {
    	this.charsetDetector = charsetDetector;
    	this.cryptManager = cryptManager;
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
	
	public ICryptManager getCryptManager() {
		return this.cryptManager;
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
