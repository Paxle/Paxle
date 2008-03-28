
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
