
package org.paxle.parser.bzip2.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.bzip2.IBzip2Parser;
import org.paxle.parser.iotools.ParserDocOutputStream;

public class Bzip2Parser implements IBzip2Parser {
	
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/x-bzip2",
			"application/bzip2",
			"application/x-bz2");
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		// read two bytes to ensure correctness and to make the CBZip2InputStream working,
		// which doesn't expect those 2 bytes at the beginning
		if (is.read() != 'B')
			throw new ParserException("input-stream for '" + location + "' is no valid BZip2-stream");
		if (is.read() != 'Z')
			throw new ParserException("input-stream for '" + location + "' is no valid BZip2-stream");
		
		final CBZip2InputStream bis = new CBZip2InputStream(is);
		final ParserContext context = ParserContext.getCurrentContext();
		final ParserDocOutputStream pdos = new ParserDocOutputStream(context.getTempFileManager(), context.getCharsetDetector());
		
		try {
			IOTools.copy(bis, pdos);			
		} finally {
			is.close();
			pdos.close();
		}
		
		IParserDocument doc = pdos.parse(location);
		doc.setStatus(IParserDocument.Status.OK);
		return doc;
	}
	
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis);
		} finally { fis.close(); }
	}
	
}
