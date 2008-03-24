package org.paxle.parser.msoffice.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.msoffice.IMsPowerpointParser;

public class MsPowerpointParser extends AMsOfficeParser implements ISubParser, IMsPowerpointParser {
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/mspowerpoint",
			"application/powerpoint",
			"application/vnd.ms-powerpoint"
	);

	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	public IParserDocument parse(URI location, String charset, InputStream fileIn)
			throws ParserException, UnsupportedEncodingException, IOException {
		CachedParserDocument parserDoc = null;
		try {		
			
			// create an empty document
			parserDoc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());
			
			// open the POI filesystem
			POIFSFileSystem fs = new POIFSFileSystem(fileIn);
			fileIn.close();
			fileIn = null;
			
			// extract metadata
			this.extractMetadata(fs, parserDoc);
			
			// extract plain text
			PowerPointExtractor parser = new PowerPointExtractor(fs);
			String text = parser.getText(true,true);
			if (text != null && text.length() > 0) {
				parserDoc.addText(text);
			}
			
			parserDoc.setStatus(IParserDocument.Status.OK);
			return parserDoc;
		} catch (Throwable e) {
			throw new ParserException(String.format("Error parsing ms-powerpoint document. %s: %s",
					e.getClass().getName(),
					e.getMessage()), e);
		}
	}

	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		
		InputStream fileIn = null;
		try {		
			// open file
			fileIn = new BufferedInputStream(new FileInputStream(content));
			return parse(location, charset, fileIn);
		} finally {
			if (fileIn != null) try { fileIn.close(); } catch (Exception e) {/* ignore this */}
		}
	}				
}
