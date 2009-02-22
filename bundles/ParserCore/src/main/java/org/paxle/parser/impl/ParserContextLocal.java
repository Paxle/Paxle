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
package org.paxle.parser.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.ISubParserManager;
import org.paxle.parser.ParserContext;

/**
 * @scr.component
 */
public class ParserContextLocal extends ThreadLocal<ParserContext> {		
	/**
	 * for logging
	 */
	protected final Log logger = LogFactory.getLog(ParserContextLocal.class);
	
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected IMimeTypeDetector mimeTypeDetector;
    
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected ICharsetDetector charsetDetector;
    
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected ISubParserManager subParserManager;
    
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected ITempFileManager tempFileManager;
    
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected IReferenceNormalizer referenceNormalizer;
    
	public ParserContextLocal() {
		ParserContext.setThreadLocal(this);
	}    
	
	@Override
	protected ParserContext initialValue() {
		return new ParserContext();
	}

	public IMimeTypeDetector getMimeTypeDetector() {
		return mimeTypeDetector;
	}

	public ICharsetDetector getCharsetDetector() {
		return charsetDetector;
	}

	public ISubParserManager getSubParserManager() {
		return subParserManager;
	}

	public ITempFileManager getTempFileManager() {
		return tempFileManager;
	}

	public IReferenceNormalizer getReferenceNormalizer() {
		return referenceNormalizer;
	}
}
