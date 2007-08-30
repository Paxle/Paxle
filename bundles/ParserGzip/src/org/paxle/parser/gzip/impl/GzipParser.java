package org.paxle.parser.gzip.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.gzip.IGzipParser;
import org.paxle.parser.iotools.ParserDocOutputStream;
import org.paxle.parser.iotools.ParserTools;

public class GzipParser implements IGzipParser {
	
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/x-gzip",
			"application/gzip");
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	public IParserDocument parse(String location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		final GZIPInputStream cfis = new GZIPInputStream(new FileInputStream(content));
		final ParserContext context = ParserContext.getCurrentContext();
		final ParserDocOutputStream pdos = new ParserDocOutputStream(context.getTempFileManager(), context.getCharsetDetector());
		try {
			ParserTools.copy(cfis, pdos);
		} finally {
			cfis.close();
			pdos.close();
		}
		return pdos.parse(location);
	}
}
