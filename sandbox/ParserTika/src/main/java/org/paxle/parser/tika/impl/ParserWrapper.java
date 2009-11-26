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
package org.paxle.parser.tika.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import javax.annotation.WillClose;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.Parser;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.IParserDocument.Status;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.ASubParser;
import org.paxle.parser.IParserContext;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.xml.sax.ContentHandler;

public class ParserWrapper extends ASubParser {
	private final String mimeType;
	private final Parser tikaParser;
	
	public ParserWrapper(String mimeType, Parser tikaParser) {
		this.mimeType = mimeType;
		this.tikaParser = tikaParser;
	}
	
	@Override
	public IParserDocument parse(URI location, String charset, @WillClose InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		try {
			final IParserContext context = ParserContext.getCurrentContext();
			final IReferenceNormalizer refNormalizer = context.getReferenceNormalizer();
			
			// creating an empty document
			final IParserDocument parserDoc = context.createDocument();
			parserDoc.setMimeType(this.mimeType);
			
			// init some helper objects
			final ContentHandler textHandler = new ParserContentHandler(refNormalizer, location, parserDoc);
			final ParserMetaData metadata = new ParserMetaData(parserDoc);
			metadata.add(Metadata.CONTENT_ENCODING, charset);
			org.apache.tika.parser.ParseContext parserContext = new org.apache.tika.parser.ParseContext();

			// parsing the content
			this.tikaParser.parse(is, textHandler, metadata, parserContext);
			
			// parsing finished
			parserDoc.setStatus(Status.OK);
			return parserDoc;
		} catch (Throwable e) {
			throw new ParserException(e.getMessage());
		} finally {
			is.close();
		}
	}
}