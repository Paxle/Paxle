package org.paxle.crawler;

import java.util.HashMap;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.crypt.md5.IMD5;

public class CrawlerContext {
    private static final ThreadLocal<CrawlerContext> context = new ThreadLocal<CrawlerContext>();
	
    private ICharsetDetector charsetDetector = null;
    private HashMap<String, Object> bag = new HashMap<String, Object>();
    private final IMD5 md5;
    
    public CrawlerContext(ICharsetDetector charsetDetector, IMD5 md5) {
    	this.charsetDetector = charsetDetector;
    	this.md5 = md5;
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
