/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.plain.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.Constants;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.IParserContext;
import org.paxle.parser.IParserContextLocal;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserException;

@Component(name=PlainParser.PID, metatype=false)
@Service(ISubParser.class)
@Property(name=ISubParser.PROP_MIMETYPES, value={"text/plain"})
public class PlainParser implements ISubParser {
	static final String PID = "org.paxle.parser.plain.impl.PlainParser";
	
	private static final int MAX_HEADLINE_LENTGH = 256;
	
	// From RFC 2396, Appendix B, changed to ensure a scheme- and host-part
	static final Pattern URI_PATTERN = Pattern.compile("([A-Za-z\\.-]+)://([^/?#]*)($|(/[^?#]*)(\\?([^#]*))?(#(.*))?)");
	private static final String PRE_FIXES = "<([{\"'_";
	private static final String POST_FIXES = ">)]}\"'_:.,;";
	
	@Reference
	protected IParserContextLocal contextLocal;
	
	static String removePrePostFixes(final String ref) {
		int l = 0, r = ref.length() - 1;
		
		while (l < r && PRE_FIXES.indexOf(ref.charAt(l)) > -1)
			l++;
		while (r > l && POST_FIXES.indexOf(ref.charAt(r)) > -1)
			r--;
		
		return ref.substring(l, r + 1);
	}
	
	static boolean parseTitle(final IParserDocument pdoc, final BufferedReader br, final IReferenceNormalizer refNorm) throws IOException {
		while (true) {
			final String headline = br.readLine();
			if (headline == null) return false;
			
			final String l = extractRefs(headline.trim(), refNorm, pdoc);
			if (l.length() > 0) {
				if (l.length() > MAX_HEADLINE_LENTGH) {
					int ws = l.lastIndexOf(' ', MAX_HEADLINE_LENTGH);
					if (ws == -1) ws = MAX_HEADLINE_LENTGH;
					
					pdoc.setTitle(l.substring(0, ws));
					pdoc.append(l.substring(ws)).append(' ');
				} else {
					pdoc.setTitle(l);
				}
				return true;
			}
		}
	}
	
	static String extractRefs(final String line, final IReferenceNormalizer refNorm, final IParserDocument pdoc) {
		final StringBuilder sb = new StringBuilder();
		final StringTokenizer st = new StringTokenizer(line, " \t\n\f\r");
		while (st.hasMoreElements()) {
			final String token = st.nextToken();
			
			boolean hasReference = false;
			if (token.indexOf("://") > 0) do {
				final Matcher m = URI_PATTERN.matcher(token);
				if (!m.find()) break;
				
				final String plainUri = removePrePostFixes(m.group(0));
				final URI uri = refNorm.normalizeReference(plainUri);
				if (uri == null) break;
				
				pdoc.addReference(uri, token, Constants.SERVICE_PID + ":" + PID);
				hasReference = true;
			} while (false);
			
			if (!hasReference)
				sb.append(token).append(' ');
		}
		return sb.toString();
	}
	
	static void parseBody(final IParserDocument pdoc, final BufferedReader br, final IReferenceNormalizer refNorm) throws IOException {
		String line;
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0)
				continue;
			
			final String l = extractRefs(line, refNorm, pdoc);
			if (l.length() > 0)
				pdoc.append(l).append(' ');
		}
	}
	
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		final IParserContext context = this.contextLocal.getCurrentContext();
		final IParserDocument pdoc = context.createDocument();
		return parse(location, charset, is, pdoc);
	}
	
	private IParserDocument parse(URI location, String charset, InputStream is, final IParserDocument pdoc)
			throws ParserException, UnsupportedEncodingException, IOException {
		final IReferenceNormalizer refNorm = this.contextLocal.getCurrentContext().getReferenceNormalizer();
		
		final BufferedReader br = new BufferedReader((charset == null)
				? new InputStreamReader(is)
				: new InputStreamReader(is, charset));
		
		if (!parseTitle(pdoc, br, refNorm)) {
			pdoc.setStatus(IParserDocument.Status.OK);
			return pdoc;
		}
		
		parseBody(pdoc, br, refNorm);
		
		pdoc.setStatus(IParserDocument.Status.OK);
		return pdoc;
	}
	
	public IParserDocument parse(URI location, String charset, File content)
			throws ParserException, UnsupportedEncodingException, IOException {
		
		final IParserContext context = this.contextLocal.getCurrentContext();
		final IParserDocument pdoc = context.createDocument();
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis, pdoc);
		} finally { fis.close(); }
	}
}
