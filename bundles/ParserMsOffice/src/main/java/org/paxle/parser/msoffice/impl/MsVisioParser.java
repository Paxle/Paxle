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

package org.paxle.parser.msoffice.impl;

import java.io.IOException;

import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.msoffice.IMsVisioParser;

public class MsVisioParser extends AMsOfficeParser implements IMsVisioParser {
	
	public MsVisioParser() {
		super("visio",
				"application/visio",
				"application/x-visio");
	}
	
	@Override
	protected void extractText(POIFSFileSystem fs, IParserDocument parserDoc) throws ParserException, IOException {
		// extract plain text
		VisioTextExtractor parser = new VisioTextExtractor(fs);
		String text = parser.getText();
		if (text != null && text.length() > 0) {
			parserDoc.addText(text);
		}			
	}
}
