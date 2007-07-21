package org.paxle.parser.gzip.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.ParserTools;
import org.paxle.parser.gzip.IGzipParser;

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
		final File uncompressed = ParserTools.createTempFile(location, GzipParser.class);
		final FileOutputStream fos = new FileOutputStream(uncompressed);
		try {
			ParserTools.copy(cfis, fos);
		} finally {
			cfis.close();
			fos.close();
		}
		return ParserTools.parse(location, uncompressed);
	}
}
