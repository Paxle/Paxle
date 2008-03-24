package org.paxle.parser.sevenzip.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ASubParser;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.sevenzip.I7zipParser;

import SevenZip.Archive.SevenZip.Handler;

public class P7zipParser extends ASubParser implements I7zipParser {
	
	private static final List<String> MimeTypes = Arrays.asList("application/x-7z-compressed");
	
	public List<String> getMimeTypes() {
		return MimeTypes;
	}
	
	public IParserDocument parse(URI location, String charset, File content) throws
			ParserException, UnsupportedEncodingException, IOException {
		final Handler archive = new Handler();
		archive.Open(new RAFInStream(content));
		
		final ParserContext context = ParserContext.getCurrentContext();
		final ITempFileManager tfm = context.getTempFileManager();
		final CachedParserDocument doc = new CachedParserDocument(tfm);
		final SZParserExtractCallback aec = new SZParserExtractCallback(location, doc, archive, tfm, context.getCharsetDetector());
		try {
			archive.Extract(null, -1, 0, aec);
			doc.setStatus(IParserDocument.Status.OK);
		} finally { 
			archive.close(); 
		}
		return doc;
	}
}
