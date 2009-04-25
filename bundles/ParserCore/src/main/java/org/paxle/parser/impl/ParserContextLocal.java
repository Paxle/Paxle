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

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.parser.IParserContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;
import org.paxle.parser.ParserContext;

/**
 * @scr.component
 */
public class ParserContextLocal extends ThreadLocal<IParserContext> {		
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
    
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected ICommandProfileManager cmdProfileManager;
	
	public ParserContextLocal() {
		ParserContext.setThreadLocal(this);
	}    
	
	@Override
	protected IParserContext initialValue() {
		return new Context();
	}
	
	private class Context implements IParserContext {
	    private HashMap<String, Object> bag = new HashMap<String, Object>();	
		
		/**
		 * @param mimeType the mime-type
		 * @return a {@link ISubParser parser} that is capable to parse a resource with the given mimetype
		 */
		public ISubParser getParser(String mimeType) {
			if (subParserManager == null) return null;
			return subParserManager.getSubParser(mimeType);
		}
		
		/**
		 * @return a class that can be used to detect the mime-type of a file. 
		 * This reference may be <code>null</code> if no {@link IMimeTypeDetector mime-type-detector} is available.
		 */
		public IMimeTypeDetector getMimeTypeDetector() {
			return mimeTypeDetector;
		}	
		
		/**
		 * @return a class that can be used to detect the charset of a resource
		 * This reference may be <code>null</code> if no {@link ICharsetDetector charset-detector} is available.
		 */
		public ICharsetDetector getCharsetDetector() {
			return charsetDetector;
		}
		
		public ITempFileManager getTempFileManager() {
			return tempFileManager;
		}
		
		public IReferenceNormalizer getReferenceNormalizer() {
			return referenceNormalizer;
		}
			
		/**
		 * TODO: currently this is an read-only {@link ICommandProfile}. We should wrap it with a transparent proxy
		 * and should flush it back to db if one of the command-profile-properties were changed.
		 */
		public ICommandProfile getCommandProfile(int profileID) {
			if (cmdProfileManager == null) return null;		
			return cmdProfileManager.getProfileByID(profileID);
		}	
		
		/**
		 * @return the {@link ICommandProfile} that belongs to the {@link Command}
		 * currently processed by the parser-worker thread
		 */
		public ICommandProfile getCommandProfile() {
			Integer profileID = (Integer) this.getProperty("cmd.profileOID");
			if (profileID == null) return null;		
			return this.getCommandProfile(profileID.intValue());
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
}
