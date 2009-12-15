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
package org.paxle.core.doc.impl.jaxb;

import java.io.IOException;

import org.paxle.core.doc.impl.AParserDocument;
import org.paxle.core.doc.impl.BasicCommand;
import org.paxle.core.doc.impl.BasicCrawlerDocument;
import org.paxle.core.doc.impl.BasicDocumentFactory;
import org.paxle.core.doc.impl.BasicIndexerDocument;
import org.paxle.core.doc.impl.BasicParserDocument;


public class JaxbFactory {
	private static BasicDocumentFactory docFactory;
	
	public static void setDocumentFactory(BasicDocumentFactory docFactory) {
		JaxbFactory.docFactory = docFactory;
	}
	
	public static BasicCommand createBasicCommand() throws IOException {
		return docFactory.createDocument(BasicCommand.class);
	}
	
	public static BasicCrawlerDocument createBasicCrawlerDocument() throws IOException {
		return docFactory.createDocument(BasicCrawlerDocument.class);
	}
	
	public static AParserDocument createAParserDocument()  throws IOException {
		throw new RuntimeException("Unable to instantiate an abstract class");
	}
	
	public static BasicParserDocument createBasicParserDocument()  throws IOException {
		return docFactory.createDocument(BasicParserDocument.class);
	}
	
	public static BasicIndexerDocument createBasicIndexerDocument()  throws IOException {
		return docFactory.createDocument(BasicIndexerDocument.class);
	}	
}
