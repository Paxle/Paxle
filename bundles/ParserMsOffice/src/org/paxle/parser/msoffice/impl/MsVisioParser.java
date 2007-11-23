package org.paxle.parser.msoffice.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.msoffice.IMsVisioParser;

public class MsVisioParser extends AMsOfficeParser implements IMsVisioParser {
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/visio",
			"application/x-visio"
	);

	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}

	public IParserDocument parse(String location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		CachedParserDocument parserDoc = null;
		
		InputStream fileIn = null;
		try {		
			// create an empty document
			parserDoc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());

			// open file			
			fileIn = new BufferedInputStream(new FileInputStream(content));		
			
			// open the POI filesystem
			POIFSFileSystem fs = new POIFSFileSystem(fileIn);
			fileIn.close();
			fileIn = null;
						
			// extract metadata
			this.extractMetadata(fs, parserDoc);
			
			// extract plain text
			VisioTextExtractor parser = new VisioTextExtractor(fs);
			String text = parser.getText();
			if (text != null && text.length() > 0) {
				parserDoc.addText(text);
			}			
			
			parserDoc.setStatus(IParserDocument.Status.OK);
			return parserDoc;
		} catch (Throwable e) {
			throw new ParserException(String.format("Error parsing ms-visio document. %s: %s",
					e.getClass().getName(),
					e.getMessage()), e);
		} finally {
			if (fileIn != null) try { fileIn.close(); } catch (Exception e) {/* ignore this */}
		}
	}	
}
