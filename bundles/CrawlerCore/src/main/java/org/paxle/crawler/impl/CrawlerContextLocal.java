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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.crypt.ICryptManager;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICommandProfileManager;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.io.IIOTools;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.ICrawlerContext;
import org.paxle.crawler.ICrawlerContextAware;
import org.paxle.crawler.ICrawlerContextLocal;
import org.paxle.parser.ISubParser;

@Component(immediate=true)
@Service(ICrawlerContextLocal.class)
@References({
	@Reference(
		name=CrawlerContextLocal.REFERENCE_SUBPARSERS, 
		referenceInterface = ISubParser.class,
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		policy=ReferencePolicy.DYNAMIC,
		bind="addSubParser",
		unbind="removeSubParser",
		target="(MimeTypes=*)"
	),
	@Reference(
		name=CrawlerContextLocal.REFERENCE_CRAWLERCONTEXT_AWARE, 
		referenceInterface = ICrawlerContextAware.class,
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		policy=ReferencePolicy.DYNAMIC,
		bind="addCrawlerContextAware",
		unbind="removeCrawlerContextAware"
	),
	@Reference(
		name=CrawlerContextLocal.REFERENCE_DOCFACTORY, 
		referenceInterface = IDocumentFactory.class,
		cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
		policy=ReferencePolicy.DYNAMIC,
		bind="addDocFactory",
		unbind="removeDocFactory",
		target="(docType=*)"
	)
})
public class CrawlerContextLocal extends ThreadLocal<ICrawlerContext> implements ICrawlerContextLocal {
	static final String REFERENCE_SUBPARSERS = "subParser";
	static final String REFERENCE_CRAWLERCONTEXT_AWARE = "crawlerContextAware";
	static final String REFERENCE_DOCFACTORY = "docFactory";
	
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();	
	
	/**
	 * The {@link ComponentContext} of this component
	 * @see #activate(ComponentContext)
	 */
	protected ComponentContext ctx;
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY, policy=ReferencePolicy.DYNAMIC)
	protected IMimeTypeDetector mimeTypeDetector;
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY, policy=ReferencePolicy.DYNAMIC)
	protected ICharsetDetector charsetDetector;
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY, policy=ReferencePolicy.DYNAMIC)
	protected ICryptManager cryptManager;
	
	@Reference(cardinality=ReferenceCardinality.MANDATORY_UNARY, policy=ReferencePolicy.DYNAMIC)
	protected ITempFileManager tempFileManager;
	    
	@Reference(cardinality=ReferenceCardinality.MANDATORY_UNARY, policy=ReferencePolicy.DYNAMIC)
	protected IIOTools ioTools;
	
	@Reference(cardinality=ReferenceCardinality.OPTIONAL_UNARY, policy=ReferencePolicy.DYNAMIC)
	protected ICommandProfileManager cmdProfileManager;	
	
	/**
	 * All {@link IDocumentFactory document-factories} registered to the system. 
	 * @see #createDocumentForInterface(Class, String)
	 */
	protected SortedSet<ServiceReference> docFactoryRefs = new TreeSet<ServiceReference>();
	
	/**
	 * A list of mime-types supported by parsers installed to the system.
	 * TODO: for a crawler-only installation we need to change this
	 */
	protected Set<String> supportedMimeTypes = Collections.synchronizedSet(new HashSet<String>());
	
	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	public CrawlerContextLocal() {
		CrawlerContext.setThreadLocal(this);
	}
	
	public ICrawlerContext getCurrentContext() {
		return this.get();
	}
	
	public void removeCurrentContext() {
		this.remove();
	}
	
	protected void activate(ComponentContext context) {
		this.ctx = context;
	}
	
	protected void addDocFactory(ServiceReference docFactory) {
		try {
			w.lock();
			this.docFactoryRefs.add(docFactory);
		} finally {
			w.unlock();
		}
	}
	
	protected void removeDocFactory(ServiceReference docFactory) {
		try {
			w.lock();
			this.docFactoryRefs.remove(docFactory);
		} finally {
			w.unlock();
		}
	}

	protected void addCrawlerContextAware(ICrawlerContextAware crawlerContextAware) {
		crawlerContextAware.setCrawlerContextLocal(this);
	}
	
	protected void removeCrawlerContextAware(ICrawlerContextAware crawlerContextAware) {
		crawlerContextAware.setCrawlerContextLocal(null);
	}
	
	protected void addSubParser(ServiceReference subParser) {
		final String[] mimeTypes = this.getSubParserMimeTypes(subParser);		
		for (String mimeType : mimeTypes) {
			this.supportedMimeTypes.add(mimeType.trim());
		}
	}
	
	protected void removeSubParser(ServiceReference subParser) {
		final String[] mimeTypes = this.getSubParserMimeTypes(subParser);		
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
	
	protected <DOC> DOC createDocumentForInterface(Class<DOC> docInterface, String filter) throws InvalidSyntaxException, IOException {
		final Filter classFilter = ctx.getBundleContext().createFilter(String.format("(%s=%s)",IDocumentFactory.DOCUMENT_TYPE,docInterface.getName()));
		final Filter propsFilter = (filter==null)?null:ctx.getBundleContext().createFilter(filter);
		
		ServiceReference factoryRef = null;
		try {
			r.lock();
			
			// loop through all doc-factories and find one that matches 
			for (ServiceReference ref : docFactoryRefs) {
				if (classFilter.match(ref) && (propsFilter == null || propsFilter.match(ref))) {
					factoryRef = ref;
					break;
				}
			}
		} finally {
			r.unlock();
		}
		
		// no factory found
		if (factoryRef == null) return null;

		// creating an document
		final IDocumentFactory factory = (IDocumentFactory) ctx.locateService(REFERENCE_DOCFACTORY, factoryRef);
		if (factory == null) return null;			
		return factory.createDocument(docInterface);
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
		
		public IIOTools getIoTools() {
			return ioTools;
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
		 * @return the {@link ICommandProfile} that belongs to the {@link ICommand}
		 * currently processed by the parser-worker thread
		 */
		public ICommandProfile getCommandProfile() {
			Integer profileID = (Integer) this.getProperty("cmd.profileOID");
			if (profileID == null) return null;		
			return this.getCommandProfile(profileID.intValue());
		}			
		
		public ICrawlerDocument createDocument() throws IOException {
			try {
				return this.createDocument(ICrawlerDocument.class, null);
			} catch (InvalidSyntaxException e) {
				// this should not occur
				throw new RuntimeException(e.getMessage());
			}
		}
		
		public <DocInterface> DocInterface createDocument(Class<DocInterface> docInterface, String filter) throws InvalidSyntaxException, IOException {
			if (docInterface == null) throw new NullPointerException("The interface-class must not be null");
			return createDocumentForInterface(docInterface, filter);
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
