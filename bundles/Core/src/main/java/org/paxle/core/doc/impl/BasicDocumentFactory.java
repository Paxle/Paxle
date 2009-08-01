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
package org.paxle.core.doc.impl;

import java.io.IOException;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;

@Component(immediate=true, metatype=false)
@Service(IDocumentFactory.class)
@Property(
	name = IDocumentFactory.DOCUMENT_TYPE,
	value = {
		"org.paxle.core.doc.ICommand",
		"org.paxle.core.doc.ICommandProfile",
		"org.paxle.core.doc.ICrawlerDocument",
		"org.paxle.core.doc.IParserDocument",
		"org.paxle.core.doc.IIndexerDocument"
	}
)
public class BasicDocumentFactory implements IDocumentFactory {
	/**
	 * A list of documents that this {@link IDocumentFactory} is capable to create.
	 */
	@SuppressWarnings("serial")
	private static final HashSet<Class<?>> SUPPORTED_CLASSES =  new HashSet<Class<?>>() {{
		add(ICrawlerDocument.class);
		add(IParserDocument.class);
		add(IIndexerDocument.class);
		add(ICommand.class);
		add(ICommandProfile.class);
	}};
	
	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	@Reference
	protected ITempFileManager tempFileManager;

	protected void activate(ComponentContext context) {
		this.logger.info(this.getClass().getSimpleName() + " registered.");
	}
	
	@SuppressWarnings("unchecked")
	public <Doc> Doc createDocument(Class<Doc> docInterface) throws IOException {
		if (docInterface == null) throw new NullPointerException("The document-interface must not be null.");
		else if (!SUPPORTED_CLASSES.contains(docInterface)) throw new IllegalArgumentException("Unsupported doc-type");
		
		if (docInterface.equals(ICrawlerDocument.class)) {		
			// currently we can simply create a new class here
			return (Doc) new BasicCrawlerDocument();
		} else if (docInterface.equals(IParserDocument.class)) {
			// return (Doc) new BasicParserDocument(this.tempFileManager);
			return (Doc) new CachedParserDocument(this.tempFileManager);
		} else if (docInterface.equals(IIndexerDocument.class)) {
			return (Doc) new BasicIndexerDocument();
		} else if (docInterface.equals(ICommand.class)) {
			return (Doc) new BasicCommand();
		} else if (docInterface.equals(ICommandProfile.class)) {
			return (Doc) new BasicCommandProfile();
		}
		
		throw new IllegalArgumentException("Unexpected doc-type");
	}

	public boolean isSupported(Class<?> docInterface) {
		if (docInterface == null) return false;
		return SUPPORTED_CLASSES.contains(docInterface);
	}
}
