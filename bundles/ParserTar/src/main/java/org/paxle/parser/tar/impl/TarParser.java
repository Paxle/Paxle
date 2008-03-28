
package org.paxle.parser.tar.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.IOTools;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.iotools.SubParserDocOutputStream;
import org.paxle.parser.tar.ITarParser;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;

public class TarParser implements ITarParser {
	
	private static final String[] MIMETYPES = {
		"application/x-tar",
		"application/x-gtar",
		"application/x-ustar",
		/* FIXME: http://filext.com/file-extension/TAR additionally lists the following. Are these valid?
		 *	multipart/x-tar 
		 *	application/x-compress 
		 *	application/x-compressed
		 */
	};
	
	public List<String> getMimeTypes() {
		return Arrays.asList(MIMETYPES);
	}
	
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis);
		} finally { fis.close(); }
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		final TarInputStream tis = new TarInputStream(is);
		final IParserDocument pdoc = new ParserDocument();
		final ParserContext context = ParserContext.getCurrentContext();
		
		TarEntry te;
		while ((te = tis.getNextEntry()) != null) {
			if (!te.isDirectory()) {
				final SubParserDocOutputStream pdos = new SubParserDocOutputStream(
						context.getTempFileManager(),
						context.getCharsetDetector(),
						pdoc, location, te.getName(), te.getSize());
				try {
					IOTools.copy(tis, pdos, te.getSize());
				} finally {
					try { pdos.close(); } catch (IOException e) {
						if (e.getCause() instanceof ParserException) {
							throw (ParserException)e.getCause();
						} else {
							throw e;
						}
					}
				}
			}
		}
		pdoc.setStatus(IParserDocument.Status.OK);
		return pdoc;
	}
}
