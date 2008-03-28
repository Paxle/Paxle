
package org.paxle.parser.msoffice.impl;

import java.io.IOException;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;
import org.paxle.parser.msoffice.IMsWordParser;

public class MsWordParser extends AMsOfficeParser implements ISubParser, IMsWordParser {
	
	public MsWordParser() {
		super("word", "application/msword");
	}
	
	@Override
	protected void extractText(POIFSFileSystem fs, IParserDocument parserDoc) throws ParserException, IOException {
		// extract plain text
		HWPFDocument doc = new HWPFDocument(fs);
		
		Range r = doc.getRange();
		for(int i=0; i<r.numParagraphs(); i++) {
			// get next paragraph 
			Paragraph p = r.getParagraph(i);
			
			// append paragraph text
			parserDoc.addText(p.text());					
		}
	}
}
