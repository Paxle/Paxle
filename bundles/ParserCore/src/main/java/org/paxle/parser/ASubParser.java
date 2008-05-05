
package org.paxle.parser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.ITempFileManager;

public abstract class ASubParser implements ISubParser {
	
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis);
		} finally { fis.close(); }
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		final ParserContext context = ParserContext.getCurrentContext();
		if (context == null)
			throw new ParserException("cannot access ParserContext whereas this method must be used from within a sub-parser");
		
		final ITempFileManager tfm = context.getTempFileManager();
		if (tfm == null)
			throw new ParserException("cannot access temp-file manager");
		
		final File content = tfm.createTempFile();
		final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(content));
		try {
			IOTools.copy(is, bos);
		} finally { bos.close(); }
		
		return parse(location, charset, content);
	}
}
