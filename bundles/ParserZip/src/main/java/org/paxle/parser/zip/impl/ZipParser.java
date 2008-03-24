package org.paxle.parser.zip.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.iotools.SubParserDocOutputStream;
import org.paxle.parser.zip.IZipParser;

public class ZipParser implements IZipParser {
	
	private static final List<String> MIME_TYPES = Arrays.asList(
			"application/zip",
			"application/x-zip",
			"application/x-zip-compressed",
			"application/java-archive");
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		final ParserContext context = ParserContext.getCurrentContext();
		final IParserDocument pdoc = new CachedParserDocument(context.getTempFileManager());
		final ZipInputStream zis = new ZipInputStream(is);
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			if (ze.isDirectory()) continue;
			final SubParserDocOutputStream sos = new SubParserDocOutputStream(
					context.getTempFileManager(),
					context.getCharsetDetector(),
					pdoc, location, ze.getName());
			try {
				IOTools.copy(zis, sos, ze.getSize());
			} finally {
				try { sos.close(); } catch (IOException e) {
					if (e.getCause() instanceof ParserException) {
						throw (ParserException)e.getCause();
					} else {
						throw e;
					}
				}
			}
		}
		
		pdoc.setStatus(IParserDocument.Status.OK);
		return pdoc;
	}
	
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis);
		} finally { 
			fis.close(); 
		}
	}
}
