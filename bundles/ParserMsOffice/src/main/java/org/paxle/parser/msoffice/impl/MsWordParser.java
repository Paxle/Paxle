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

package org.paxle.parser.msoffice.impl;

import java.io.IOException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;

@Component(metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={"application/msword"})
public class MsWordParser extends AMsOfficeParser implements ISubParser {
	
	public MsWordParser() {
		super("word");
	}
	
	@Override
	protected void extractText(POIFSFileSystem fs, IParserDocument parserDoc) throws ParserException, IOException {
		// extract plain text
		final HWPFDocument doc = new HWPFDocument(fs);
		
		final Range r = doc.getRange();
		for(int i=0; i<r.numParagraphs(); i++) {
			// get next paragraph 
			final Paragraph p = r.getParagraph(i);
			
			// append paragraph text
			parserDoc.append(p.text());
			// we know that this is the end of a block of text, so we can include a separator
			parserDoc.append(' ');
		}
	}
}
