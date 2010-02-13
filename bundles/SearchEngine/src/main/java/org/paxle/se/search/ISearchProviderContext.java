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

package org.paxle.se.search;

import java.io.IOException;

import org.osgi.framework.InvalidSyntaxException;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;

/**
 * @since 0.1.5-SNAPSHOT
 */
public interface ISearchProviderContext {
	/**
	 * A function to create a new and empty {@link IIndexerDocument} via 
	 * one of the {@link IDocumentFactory document-factories} registered to the system. 
	 * @return a newly created {@link IIndexerDocument}
	 */
	public IIndexerDocument createDocument() throws IOException;
	
	/**
	 * A function to create a new and empty document of the given type. The second arguments can be used
	 * to select between different {@link IDocumentFactory document-factories} generating the same type.
	 * 
	 * @param docInterface that interface of the document to be created
	 * @param props
	 * @return a newly created document of the given type
	 */
	public <DocInterface> DocInterface createDocument(Class<DocInterface> docInterface, String filter) throws InvalidSyntaxException, IOException;
}
