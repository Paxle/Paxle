package org.paxle.parser.sevenzip.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.sevenzip.I7zipParser;

import SevenZip.Archive.SevenZip.Handler;

public class P7zipParser implements I7zipParser {
	
	private static final List<String> MimeTypes = Arrays.asList("application/x-7z-compressed");
	
	public List<String> getMimeTypes() {
		return MimeTypes;
	}
	
	public IParserDocument parse(String location, String charset, File content) throws
		ParserException, UnsupportedEncodingException, IOException {
		final Handler archive = new Handler();
		archive.Open(new RAFInStream(content));
		
		final CachedParserDocument doc = new CachedParserDocument();
		final SZParserExtractCallback aec = new SZParserExtractCallback(doc, archive);
		try {
			archive.Extract(null, -1, 0, aec);
		} finally { archive.close(); }
		return doc;
	}
}
