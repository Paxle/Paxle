
package org.paxle.parser.plain.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.plain.IPlainParser;

public class PlainParser implements IPlainParser {
	
	private static final int MAX_HEADLINE_LENTGH = 256;
	
	// From RFC 2396, Appendix B, changed to ensure a scheme- and host-part
	private static final Pattern URI_PATTERN = Pattern.compile("^([^:/?#]+)://([^/?#]*)([^?#]*)(\\?([^#]*))?(#(.*))?");
	
	private static final List<String> MIME_TYPES = Arrays.asList("text/plain");
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	static boolean parseTitle(final IParserDocument pdoc, final BufferedReader br) throws IOException {
		while (true) {
			String headline = br.readLine();
			if (headline == null)
				return false;
			
			headline = headline.trim();
			if (headline.length() != 0) {
				final Matcher m = URI_PATTERN.matcher(headline);
				if (m.matches()) {
					pdoc.addReference(headline, headline);
				} else {
					if (headline.length() > MAX_HEADLINE_LENTGH) {
						int ws = headline.lastIndexOf(' ', MAX_HEADLINE_LENTGH);
						if (ws == -1)
							ws = MAX_HEADLINE_LENTGH;
						pdoc.setTitle(headline.substring(0, ws));
						pdoc.addText(headline.substring(ws));
						pdoc.addText(" ");
					} else {
						pdoc.setTitle(headline);
					}
					return true;
				}
			}
		}
	}
	
	static void parseBody(final IParserDocument pdoc, final BufferedReader br) throws IOException {
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			
			final StringTokenizer st = new StringTokenizer(line, " \t\n\\x0B\f\r");
			while (st.hasMoreTokens()) {
				final String token = st.nextToken();
				final Matcher m = URI_PATTERN.matcher(token);
				if (m.matches()) {
					pdoc.addReference(token, token);
				} else {
					pdoc.addText(token);
					pdoc.addText(" ");
				}
			}
		}
	}
	
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		
		final ParserContext context = ParserContext.getCurrentContext();
		final IParserDocument pdoc = new CachedParserDocument((int)Math.min(content.length(), 1 << 20), context.getTempFileManager());
		
		final FileInputStream fis = new FileInputStream(content);
		try {
			final BufferedReader br = new BufferedReader((charset == null)
					? new InputStreamReader(fis)
					: new InputStreamReader(fis, charset));
			
			if (!parseTitle(pdoc, br)) {
				pdoc.setStatus(IParserDocument.Status.OK);
				return pdoc;
			}
			
			parseBody(pdoc, br);
			
			pdoc.setStatus(IParserDocument.Status.OK);
			return pdoc;
		} finally { fis.close(); }
	}
}
