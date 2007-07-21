package org.paxle.parser.bzip2.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.bzip2.CBZip2InputStream;
import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserException;
import org.paxle.parser.ParserTools;
import org.paxle.parser.bzip2.IBzip2Parser;


public class Bzip2Parser implements IBzip2Parser {
	
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/x-bzip2",
			"application/bzip2",
			"application/x-bz2");
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	public IParserDocument parse(String location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		// read two bytes to ensure correctness and to make the CBZip2InputStream working,
		// which doesn't expect those 2 bytes at the beginning
		if (fis.read() != 'B')
			throw new ParserException("file '" + content + "' is no valid BZip2-file");
		if (fis.read() != 'Z')
			throw new ParserException("file '" + content + "' is no valid BZip2-file");
		
		final CBZip2InputStream is = new CBZip2InputStream(fis);
		final File uncompressed = ParserTools.createTempFile(location, Bzip2Parser.class);
		final FileOutputStream fos = new FileOutputStream(uncompressed);
		try {
			ParserTools.copy(is, fos);
		} finally {
			is.close();
			fos.close();
		}
		return ParserTools.parse(location, uncompressed);
	}
	
}
