package org.paxle.parser;

import java.util.HashMap;

import org.paxle.parser.impl.SubParserManager;

public class ParserContext {
    private static ThreadLocal<ParserContext> context = new ThreadLocal<ParserContext>();
	
    private SubParserManager subParserManager = null;
    private HashMap<String, Object> bag = new HashMap<String, Object>();
    
    public ParserContext(SubParserManager subParserManager) {
    	this.subParserManager = subParserManager;
	}
    
    public static void setCurrentContext(ParserContext parserContext) {
    	context.set(parserContext);
    }    
    
	public static ParserContext getCurrentContext() {
		return context.get();
	}
	
	public ISubParser getParser(String mimeType) {
		return this.subParserManager.getSubParser(mimeType);
	}
	
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
