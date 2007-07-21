package org.paxle.parser.sevenzip.impl;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.sevenzip.I7zipParser;

import SevenZip.Archive.SevenZip.Handler;

public class P7zipParser implements I7zipParser {
	
	private static final List<String> MimeTypes = Arrays.asList("application/x-7z-compressed");
	
	public List<String> getMimeTypes() {
		return MimeTypes;
	}
	
	public IParserDocument parse(String location, String charset, File content) throws ParserException {
		final Handler archive = new Handler();
        try {
            archive.Open(new RAFInStream(content));
        } catch (IOException e) { throw new ParserException("error opening 7zip archive", e); }
        
        final ParserDocument doc = new ParserDocument();
        final SZParserExtractCallback aec = new SZParserExtractCallback(doc, archive);
        try {
            archive.Extract(null, -1, 0, aec);
            return doc;
        } catch (IOException e) {
        	throw new ParserException("error processing 7zip archive at internal file: "
        			+ aec.getCurrentFilePath(), e);
        } finally {
        	try { archive.close(); } catch (IOException e) {  }
        }
	}
}
