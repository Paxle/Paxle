/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.se.search.impl;

import java.io.IOException;
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
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchProviderContext;
import org.paxle.se.search.SearchProviderContext;

@Component(immediate=true)
@Reference(
	name="docFactory", 
	referenceInterface = IDocumentFactory.class,
	cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE,
	policy=ReferencePolicy.DYNAMIC,
	bind="addDocFactory",
	unbind="removeDocFactory",
	target="(docType=*)"
)
public class SearchProviderContextLocal extends ThreadLocal<ISearchProviderContext> {
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock();	
	
	/**
	 * The {@link ComponentContext} of this component
	 * @see #activate(ComponentContext)
	 */
	protected ComponentContext ctx;	
	
	/**
	 * for logging
	 */
	protected final Log logger = LogFactory.getLog(this.getClass());	
	
	/**
	 * All {@link IDocumentFactory document-factories} registered to the system. 
	 * @see #createDocumentForInterface(Class, String)
	 */
	protected SortedSet<ServiceReference> docFactoryRefs = new TreeSet<ServiceReference>();	
	
	public SearchProviderContextLocal() {
		SearchProviderContext.setThreadLocal(this);
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
	
	@Override
	protected ISearchProviderContext initialValue() {
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
		final IDocumentFactory factory = (IDocumentFactory) ctx.locateService("docFactory", factoryRef);
		if (factory == null) return null;			
		return factory.createDocument(docInterface);
	}		
	
	private class Context implements ISearchProviderContext {
		public IIndexerDocument createDocument() throws IOException {
			try {
				return this.createDocument(IIndexerDocument.class, null);
			} catch (InvalidSyntaxException e) {
				// this should not occur
				throw new RuntimeException(e.getMessage());
			}
		}
		
		public <DocInterface> DocInterface createDocument(Class<DocInterface> docInterface, String filter) throws InvalidSyntaxException, IOException {
			if (docInterface == null) throw new NullPointerException("The interface-class must not be null");
			return createDocumentForInterface(docInterface, filter);
		}		
	}
}
