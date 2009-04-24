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
package org.paxle.crawler.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.ServiceReference;
import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.ICrawlerContext;

/**
 * @scr.component
 * @scr.reference name="subParser" 
 * 				  interface="org.paxle.parser.ISubParser" 
 * 				  cardinality="0..n" 
 * 				  policy="dynamic" 
 * 				  bind="addSubParser" 
 * 				  unbind="removeSubParser"
 * 				  target="(MimeTypes=*)
 */
public class CrawlerContextLocal extends ThreadLocal<ICrawlerContext> {
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
	protected ICryptManager cryptManager;
	
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected ITempFileManager tempFileManager;
	    
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic" 
	 */
	protected ICommandProfileManager cmdProfileManager;	
	
	protected Set<String> supportedMimeTypes = Collections.synchronizedSet(new HashSet<String>());
	
	public CrawlerContextLocal() {
		CrawlerContext.setThreadLocal(this);
	}
	
	protected void addSubParser(ServiceReference subParser) {
		String[] mimeTypes = this.getSubParserMimeTypes(subParser);		
		for (String mimeType : mimeTypes) {
			this.supportedMimeTypes.add(mimeType.trim());
		}
	}
	
	public void removeSubParser(ServiceReference subParser) {
		String[] mimeTypes = this.getSubParserMimeTypes(subParser);		
		for (String mimeType : mimeTypes) {
			this.supportedMimeTypes.remove(mimeType.trim());
		}
	}
	
	private String[] getSubParserMimeTypes(ServiceReference reference) {
		String[] mimeTypes = {};
		Object mimeTypesProp = reference.getProperty("MimeTypes");
		if (mimeTypesProp instanceof String) mimeTypes = new String[]{(String)mimeTypesProp};
		else if (mimeTypesProp instanceof String[]) mimeTypes = (String[]) mimeTypesProp;
		return mimeTypes;
	}	
	
	@Override
	protected ICrawlerContext initialValue() {
		return new Context();
	}

	private class Context implements ICrawlerContext {
		private final HashMap<String, Object> bag = new HashMap<String, Object>();
		
		
		/**
		 * @return a class that can be used to detect the charset of a resource
		 *         This reference may be <code>null</code> if no 
		 *         {@link ICharsetDetector charset-detector} is available.
		 */
		public ICharsetDetector getCharsetDetector() {
			return charsetDetector;
		}
		
		public ICryptManager getCryptManager() {
			return  cryptManager;
		}
		
		public ITempFileManager getTempFileManager() {
			return tempFileManager;
		}
		
		/**
		 * @return a class that can be used to detect the mime-type of a resource
		 * 	       This reference may be <code>null</code> if no 
		 *         {@link IMimeTypeDetector mimetype-detector} is available.
		 */
		public IMimeTypeDetector getMimeTypeDetector() {
			return mimeTypeDetector;
		}
		
		/**
		 * @return a set of mime-types supported by the 
		 * 		   {@link org.paxle.parser.ISubParser subparsers} that are 
		 *         currently registered on the system.
		 */
		public Set<String> getSupportedMimeTypes() {
			return supportedMimeTypes;
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
