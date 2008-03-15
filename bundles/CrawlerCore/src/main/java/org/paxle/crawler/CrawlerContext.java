package org.paxle.crawler;

import java.util.HashMap;
import java.util.Set;

import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.crawler.impl.CrawlerContextLocal;

public class CrawlerContext {
    
	private static CrawlerContextLocal context = null;
    private final HashMap<String, Object> bag = new HashMap<String, Object>();   
    
	public static void setThreadLocal(CrawlerContextLocal threadLocal) {
		context = threadLocal;
	}
    
	public static CrawlerContext getCurrentContext() {
		return context.get();		
	}	
	
	public static void removeCurrentContext() {
		context.remove();
	}
	
	/**
	 * @return a class that can be used to detect the charset of a resource
	 *         This reference may be <code>null</code> if no 
	 *         {@link ICharsetDetector charset-detector} is available.
	 */
	public ICharsetDetector getCharsetDetector() {
		return CrawlerContext.context.getCharsetDetector();
	}
	
	public ICryptManager getCryptManager() {
		return CrawlerContext.context.getCryptManager();
	}
	
	public ITempFileManager getTempFileManager() {
		return CrawlerContext.context.getTempFileManager();
	}
	
	/**
	 * @return a class that can be used to detect the mime-type of a resource
	 * 	       This reference may be <code>null</code> if no 
	 *         {@link IMimeTypeDetector mimetype-detector} is available.
	 */
	public IMimeTypeDetector getMimeTypeDetector() {
		return CrawlerContext.context.getMimeTypeDetector();
	}
	
	/**
	 * @return a set of mime-types supported by the 
	 * 		   {@link org.paxle.parser.ISubParser subparsers} that are 
	 *         currently registered on the system.
	 */
	public Set<String> getSupportedMimeTypes() {
		return CrawlerContext.context.getSupportedMimeTypes();
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
