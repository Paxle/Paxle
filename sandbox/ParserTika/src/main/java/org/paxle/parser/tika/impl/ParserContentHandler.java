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

package org.paxle.parser.tika.impl;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.sax.BodyContentHandler;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.norm.IReferenceNormalizer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ParserContentHandler extends BodyContentHandler {
	final Log logger = LogFactory.getLog(this.getClass());
	final IReferenceNormalizer refNormalizer;
	final IParserDocument parserDoc;
	final Writer parserDocWriter;
	final URI baseLocation;
	
	URI currentLink;
	StringBuilder currentLinkText = new StringBuilder();
	
	public ParserContentHandler(IReferenceNormalizer refNormalizer, URI location, IParserDocument parserDoc) throws IOException {
		super(parserDoc.getTextWriter());
		this.refNormalizer = refNormalizer;
		this.baseLocation = location;
		this.parserDoc = parserDoc;
		this.parserDocWriter = parserDoc.getTextWriter();
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (localName.equalsIgnoreCase("a") && attributes.getValue("href") != null) {
			final String href = attributes.getValue("href");
			this.currentLink = this.generateURI(href);
		} 
		super.startElement(uri, localName, qName, attributes);
	}
	
	@Override
	public void characters(char[] chars, int start, int length) throws SAXException {
		if (currentLink != null) {
			this.currentLinkText.append(chars, start, length);
		}
		super.characters(chars, start, length);
	}		
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (currentLink != null) {
			this.parserDoc.addReference(this.currentLink, this.currentLinkText.toString(), "HtmlParser");
			this.currentLink = null;
			this.currentLinkText.setLength(0);
		}
		super.endElement(uri, localName, qName);
	}
	
	private URI generateURI(String href) {
		if (href == null) return null;
		else if (href.toLowerCase().startsWith("javascript")) return null;
		else if (href.toLowerCase().startsWith("mailto")) return null;
		else if (href.startsWith("#")) return null;
		
		try {				
			// cut of tailing #
			int pos = href.indexOf('#');
			if (pos != -1) {
				href = href.substring(0,pos);
			}
			
			// build the URI
			URI uri = null;
			if (!href.contains(":/")) {					
				uri = baseLocation.resolve(href);
			} else if (!href.toLowerCase().startsWith("javascript")) {
				uri = new URI(href);
			}
			
			// Normalize the URI
			if (uri != null) {
				return this.refNormalizer.normalizeReference(uri.toString());
			}
		} catch (Throwable e) {
			this.logger.warn(String.format(
				"Document '%s' contains an invalid href '%s': %s",
				baseLocation,
				href,
				e.getMessage()
			));
		}
		return null;
	}		
	
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
		try {
			this.parserDocWriter.flush();
		} catch (IOException e) {
			this.logger.error(String.format(
				"Unexpected %s while parsing %s.",
				e.getClass().getName(),
				this.baseLocation
			),e);
		}
	}
}