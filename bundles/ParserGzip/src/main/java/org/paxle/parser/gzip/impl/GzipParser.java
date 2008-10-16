package org.paxle.parser.gzip.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.gzip.IGzipParser;
import org.paxle.parser.iotools.ParserDocOutputStream;

public class GzipParser implements IGzipParser {
	
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/x-gzip",
			"application/gzip");
	
	/**
	 * @see ISubParser#getMimeTypes()
	 */
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	/**
	 * @see ISubParser#parse(URI, String, InputStream)
	 */
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		final GZIPInputStream cfis = new GZIPInputStream(is);
		final ParserContext context = ParserContext.getCurrentContext();
		final ParserDocOutputStream pdos = new ParserDocOutputStream(context.getTempFileManager(), context.getCharsetDetector());
		try {
			IOTools.copy(cfis, pdos);
		} finally {
			cfis.close();
			pdos.close();
		}
		
		IParserDocument doc = pdos.parse(location);
		doc.setStatus(IParserDocument.Status.OK);
		return doc;
	}
	
	/**
	 * @see ISubParser#parse(URI, String, File)
	 */
	public IParserDocument parse(URI location, String charset, File content) throws ParserException, UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis);
		} finally { fis.close(); }
	}
}
