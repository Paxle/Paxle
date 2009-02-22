/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
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
import org.paxle.parser.impl.ParserContextLocal;

public class ParserContext {	
	private static ParserContextLocal context = null;

    private HashMap<String, Object> bag = new HashMap<String, Object>();	
	    
	public static void setThreadLocal(ParserContextLocal threadLocal) {
		context = threadLocal;
	}
	
	public static ParserContext getCurrentContext() {
		return context.get();		
	}	
	
	public static void removeCurrentContext() {
		context.remove();
	}

	/**
	 * @param mimeType the mime-type
	 * @return a {@link ISubParser parser} that is capable to parse a resource with the given mimetype
	 */
	public ISubParser getParser(String mimeType) {
		ISubParserManager subParserManager = context.getSubParserManager();
		if (subParserManager == null) return null;
		return subParserManager.getSubParser(mimeType);
	}
	
	/**
	 * @return a class that can be used to detect the mime-type of a file. 
	 * This reference may be <code>null</code> if no {@link IMimeTypeDetector mime-type-detector} is available.
	 */
	public IMimeTypeDetector getMimeTypeDetector() {
		return context.getMimeTypeDetector();
	}	
	
	/**
	 * @return a class that can be used to detect the charset of a resource
	 * This reference may be <code>null</code> if no {@link ICharsetDetector charset-detector} is available.
	 */
	public ICharsetDetector getCharsetDetector() {
		return context.getCharsetDetector();
	}
	
	public ITempFileManager getTempFileManager() {
		return context.getTempFileManager();
	}
	
	public IReferenceNormalizer getReferenceNormalizer() {
		return context.getReferenceNormalizer();
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
