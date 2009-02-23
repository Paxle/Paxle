/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.xbel.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ASubParser;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.xbel.api.Bookmark;
import org.paxle.parser.xbel.api.Folder;
import org.paxle.parser.xbel.api.Xbel;

/**
 * @scr.component
 * @scr.service interface="org.paxle.parser.ISubParser"
 * @scr.property name="MimeTypes" private="true" 
 * 				 values.1="application/xbel+xml"
 * 				 values.2="application/x-xbel"
 */
public class XbelParser extends ASubParser implements ISubParser {

	private final JAXBContext jaxbContext;
	
	public XbelParser() throws JAXBException {
		this.jaxbContext = JAXBContext.newInstance("org.paxle.parser.xbel.api");
	}

	@Override
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		
		try {
			// creating an empty parser document 
			final IParserDocument pdoc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());
			
			// parsing the xbel stream
			Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
			Xbel xbelDoc = (Xbel) unmarshaller.unmarshal(is);
			
			String title = xbelDoc.getTitle();
			if (title != null) pdoc.setTitle(title);
			
			String desc = xbelDoc.getDesc();
			if (desc != null) pdoc.addText(desc + "\r\n");
			
			// loop through the bookmarks/folters
			List<Object> items = xbelDoc.getBookmarkOrFolderOrAliasOrSeparator();
			this.extract(items, pdoc);
			
			pdoc.setStatus(IParserDocument.Status.OK);
			return pdoc;
		} catch (Throwable e) {
			throw new ParserException("Unable to parse xbel document", e);
		}
	}
	
	private void extract(Folder folder, IParserDocument pdoc) throws IOException {
		if (folder == null) return;
		
		String title = folder.getTitle();
		if (title != null) pdoc.addText(title + ":" + "\r\n");
		
		String descr = folder.getDesc();
		if (descr != null) pdoc.addText(descr + "\r\n");
		
		List<Object> items = folder.getBookmarkOrFolderOrAliasOrSeparator();
		this.extract(items, pdoc);
	}
	
	private void extract(Bookmark bm, IParserDocument pdoc) throws IOException {
		if (bm == null) return;
		
		String title = bm.getTitle();
		String url = bm.getHref();
		if (url != null) {
			pdoc.addReference(URI.create(url), title==null?url:title, "ParserXbel");
		}
		
		String desc = bm.getDesc();
		if (desc != null) pdoc.addText(desc + "\r\n");
	}
	
	private void extract(List<Object> items, IParserDocument pdoc) throws IOException {
		if (items == null) return;

		for (Object item : items) {
			if (item instanceof Folder) {
				this.extract((Folder) item, pdoc);
			} else if (item instanceof Bookmark) {
				this.extract((Bookmark)item, pdoc);
			}
		}

	}
}
