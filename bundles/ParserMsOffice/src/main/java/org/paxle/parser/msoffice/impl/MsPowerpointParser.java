
package org.paxle.parser.msoffice.impl;

import java.io.IOException;

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;
import org.paxle.parser.msoffice.IMsPowerpointParser;

public class MsPowerpointParser extends AMsOfficeParser implements ISubParser, IMsPowerpointParser {
	
	public MsPowerpointParser() {
		super("powerpoint",
			"application/mspowerpoint",
			"application/powerpoint",
			"application/vnd.ms-powerpoint");
	}
	
	@Override
	protected void extractText(POIFSFileSystem fs, IParserDocument parserDoc) throws ParserException, IOException {
		// extract plain text
		PowerPointExtractor parser = new PowerPointExtractor(fs);
		String text = parser.getText(true,true);
		if (text != null && text.length() > 0) {
			parserDoc.addText(text);
		}
	}
}
