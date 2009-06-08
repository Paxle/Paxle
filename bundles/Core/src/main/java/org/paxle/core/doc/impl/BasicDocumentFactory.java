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

import javax.annotation.Nonnull;

import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;

public class BasicDocumentFactory implements IDocumentFactory {
	@SuppressWarnings("serial")
	private static final HashSet<Class<?>> SUPPORTED_CLASSES =  new HashSet<Class<?>>() {{
		add(ICrawlerDocument.class);
		add(IParserDocument.class);
		add(IIndexerDocument.class);
		add(ICommand.class);
		add(ICommandProfile.class);
	}};
	
	private final ITempFileManager tempFileManager;
	
	public BasicDocumentFactory(@Nonnull ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
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
		return SUPPORTED_CLASSES.contains(docInterface);
	}
}
