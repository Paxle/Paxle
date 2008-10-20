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

package org.paxle.parser;

import java.util.HashMap;

import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;

public class ParserContext {
	
    private static final ThreadLocal<ParserContext> context = new ThreadLocal<ParserContext>();
	
    private final IMimeTypeDetector mimeTypeDetector;
    private final ICharsetDetector charsetDetector;
    private final ISubParserManager subParserManager;
    private final ITempFileManager tempFileManager;
    private final IReferenceNormalizer referenceNormalizer;
    
    private HashMap<String, Object> bag = new HashMap<String, Object>();
    
    public ParserContext(
    		ISubParserManager subParserManager,
    		IMimeTypeDetector mimeTypeDetector,
    		ICharsetDetector charsetDetector,
    		ITempFileManager tempFileManager,
    		IReferenceNormalizer referenceNormalizer) {
    	this.subParserManager = subParserManager;
    	this.mimeTypeDetector = mimeTypeDetector;
    	this.charsetDetector = charsetDetector;
    	this.tempFileManager = tempFileManager;
    	this.referenceNormalizer = referenceNormalizer;
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
	
	public ITempFileManager getTempFileManager() {
		return this.tempFileManager;
	}
	
	public IReferenceNormalizer getReferenceNormalizer() {
		return this.referenceNormalizer;
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
