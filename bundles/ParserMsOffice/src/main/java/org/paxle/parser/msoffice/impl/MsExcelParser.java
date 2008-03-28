
package org.paxle.parser.msoffice.impl;

import java.io.IOException;

import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.msoffice.IMsExcelParser;

public class MsExcelParser extends AMsOfficeParser implements IMsExcelParser {
	
	public MsExcelParser() {
		super("excel",
				"application/msexcel",
				"application/vnd.ms-excel");
	}
	
	@Override
	protected void extractText(POIFSFileSystem fs, IParserDocument parserDoc) throws ParserException, IOException {
		final ExcelExtractor extractor = new ExcelExtractor(fs);
		final String text = extractor.getText();
		if (text != null && text.length() > 0)
			parserDoc.addText(text);
	}
}
