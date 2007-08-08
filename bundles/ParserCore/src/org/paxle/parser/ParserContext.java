package org.paxle.parser;

import java.util.HashMap;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.parser.impl.SubParserManager;

public class ParserContext {
    private static final ThreadLocal<ParserContext> context = new ThreadLocal<ParserContext>();
	
    private IMimeTypeDetector mimeTypeDetector = null;
    private ICharsetDetector charsetDetector = null;
    private SubParserManager subParserManager = null;
    private HashMap<String, Object> bag = new HashMap<String, Object>();
    
    public ParserContext(SubParserManager subParserManager, IMimeTypeDetector mimeTypeDetector, ICharsetDetector charsetDetector) {
    	this.subParserManager = subParserManager;
    	this.mimeTypeDetector = mimeTypeDetector;
    	this.charsetDetector = charsetDetector;
	}
    
    public static void setCurrentContext(ParserContext parserContext) {
    	context.set(parserContext);
    }    
    
	public static ParserContext getCurrentContext() {
		return context.get();
	}
	
	/**
	 * @param mimeType the mime-type
	 * @return a {@link ISubParser parser} that is capable to parse a resource with the given mimetype
	 */
	public ISubParser getParser(String mimeType) {
		return this.subParserManager.getSubParser(mimeType);
	}
	
	/**
	 * @return a class that can be used to detect the mime-type of a file. 
	 * This reference may be <code>null</code> if no {@link IMimeTypeDetector mime-type-detector} is available.
	 */
	public IMimeTypeDetector getMimeTypeDetector() {
		return this.mimeTypeDetector;
	}	
	
	/**
	 * @return a class that can be used to detect the charset of a resource
	 * This reference may be <code>null</code> if no {@link ICharsetDetector charset-detector} is available.
	 */
	public ICharsetDetector getCharsetDetector() {
		return this.charsetDetector;
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
