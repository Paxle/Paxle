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
package org.paxle.crawler;

import java.util.HashMap;
import java.util.Set;

import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
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
	
	/**
	 * TODO: currently this is an read-only {@link ICommandProfile}. We should wrap it with a transparent proxy
	 * and should flush it back to db if one of the command-profile-properties were changed.
	 */
	public ICommandProfile getCommandProfile(int profileID) {
		ICommandProfileManager pm = context.getCommandProfileManager();
		if (pm == null) return null;		
		return pm.getProfileByID(profileID);
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
