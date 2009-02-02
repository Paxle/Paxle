/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.core.norm.impl;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.core.queue.ICommand;

public class ReferenceNormalizer implements IReferenceNormalizer, IFilter<ICommand> {
	
	private static final class QueryEntry implements Comparable<QueryEntry> {
		
		public final String key;
		public final String val;
		
		public QueryEntry(final String key, final String val) {
			this.key = key;
			this.val = val;
		}
		
		public int compareTo(QueryEntry o) {
			return key.compareTo(o.key);
		}
	}
	
	public static final Charset UTF8 = Charset.forName("UTF-8");
	
	public static final Hashtable<String,Integer> DEFAULT_PORTS = new Hashtable<String,Integer>();
	static {
		// HTTP(S) is not part of the global url-stream-handlers
		DEFAULT_PORTS.put("http", Integer.valueOf(80));
		DEFAULT_PORTS.put("https", Integer.valueOf(443));
	}
	
	private final Log logger = LogFactory.getLog(ReferenceNormalizer.class);
	private final boolean sortQuery;
	private final boolean appendSlash;
	private final boolean includePort;
	
	public ReferenceNormalizer() {
		this(false, false, false);
	}
	
	/**
	 * Creates a new normalization filter for references.
	 * @param sortQuery whether to sort the query parameters lexicographically by their respective keys
	 * @param appendSlash whether a slash should be appended to the path if the last path element does not contain a dot
	 * @param includePort whether the port-number should be included by default
	 * @see <a href="http://dblab.ssu.ac.kr/publication/LeKi05a.pdf">On URL Normalization</a>
	 */
	public ReferenceNormalizer(final boolean sortQuery, final boolean appendSlash, final boolean includePort) {
		this.sortQuery = sortQuery;
		this.appendSlash = appendSlash;
		this.includePort = includePort;
	}
	
	public int getDefaultPort(String protocol) {
		final Integer port = DEFAULT_PORTS.get(protocol);
		return (port == null) ? -1 : port.intValue();
	}
	
	public URI normalizeReference(String reference) {
		return normalizeReference(reference, UTF8);
	}
	
	public URI normalizeReference(String reference, Charset charset) {
		try {
			// temporary "solution" until I've found a way to escape characters in different charsets than UTF-8
			URI uri;
			// uri = new URI(reference);
			uri = parseBaseUrlString(reference, charset);
			uri = uri.normalize();		// resolve backpaths
			return uri;
		} catch (URISyntaxException e) {
			logger.warn(String.format("Error normalizing reference: %s", e.getMessage()));
			return null;
		}
	}
	
	private static final Pattern PORT_PATTERN = Pattern.compile("\\d{1,5}");
	
	/**
	 * This method takes an URL as input and returns it in a normalized form. There should be no change in the functionality of the URL.
	 * @param location the unnormalized URL
	 * @param charset the {@link Charset} of the document this link was extracted from. It is needed to transform URL-encoded entities
	 *        to Java's Unicode representation. If <code>null</code> is passed, the default charset will be used
	 * @return the normalized URL consisting of protocol, username/password if given, hostname, port if it is not the default port for
	 *         its protocol, the path and all given query arguments. The fragment part is omitted. It also performs Punycode- and
	 *         URL-decoding.
	 * @author Roland Ramthun, Franz Brau&szlig;e
	 * @throws MalformedURLException if the given URL-String could not be parsed due to inconsistency with the URI-standard
	 * 
	 * FIXME doesn't handle unescaped characters in other charsets than UTF-8 correctly
	 */
	public URI parseBaseUrlString(final String url, final Charset charset) throws URISyntaxException {
		/*
		 * What happens here?
		 * 
		 * Every URL has to end with a slash, if it only consists of a scheme and authority.
		 * 
		 * This slash is a part of the path. Even a simple URL like "http://example.org/" is a mapping on a directory on the server.
		 * As directories have to end with a slash, there _must_ be a slash if there is no path given ending with a filename.
		 * 
		 * In the next step we will remove default ports from the URL.
		 * 
		 * Then we convert the protocol identifier and the (sub-) domain to lowercase.
		 * Case is not important for these parts, for the path it is.
		 * 
		 * Then we resolve backpaths in the path.
		 * 
		 * Later the resulting normalized URL is assembled, along this way possible fragments are removed, as they are simply not added
		 */
		
		// init
		String protocol = null;
		String username = null;
		String password = null;
		String host = null;
		int port = -1;
		String path = "/";
		String query = null;
		// String fragment = null;
		
		// extract the protocol
		int urlStart = 0;
		int colonpos = url.indexOf(':', urlStart);
		if (colonpos <= 0)
			throw new URISyntaxException(url, "No protocol specified");
		protocol = url.substring(0, colonpos).toLowerCase();
		
		final int protocolEnd;
		if (url.length() > (colonpos + 1) && url.charAt(colonpos + 1) == '/' && url.charAt(colonpos + 2) == '/') {
			protocolEnd = colonpos + 3;
		} else {
			throw new URISyntaxException(url, "No valid protocol identifier given");
		}
		
		// extract username / password
		final int slashAfterHost = url.indexOf('/', protocolEnd);
		final int at = url.indexOf('@', protocolEnd);
		final int hostStart;
		final int credSepColon = url.indexOf(':', protocolEnd); //the colon which separates username and password
		if (at != -1 && at < slashAfterHost) {
			if (credSepColon > (protocolEnd + 1) && credSepColon < at) {
				username = url.substring(protocolEnd, credSepColon);
				password = url.substring(credSepColon + 1, at);
			} else {
				username = url.substring(protocolEnd, at);
			}
			hostStart = at + 1;
		} else {
			hostStart = protocolEnd;
		}
		
		// extract the hostname
		final int portColon = url.indexOf(':', hostStart);
		final int hostEndTmp = (slashAfterHost == -1) ? url.length() : slashAfterHost;
		final int hostEnd = (portColon > hostStart && portColon < hostEndTmp) ? portColon : hostEndTmp;
		// TODO: de-punycode
		// host = IDN.toUnicode(url.substring(hostStart, hostEnd).toLowerCase());		// de-punycode (sub-)domain(s) - java 1.6 code
		host = url.substring(hostStart, hostEnd).toLowerCase();
		
		// extract the port
		final int portEnd = (slashAfterHost == -1) ? url.length() : slashAfterHost;
		if (portColon != -1 && portColon < portEnd) {
			final String portNr = url.substring(portColon + 1, portEnd);
			if (!PORT_PATTERN.matcher(portNr).matches())
				throw new URISyntaxException(url, "Illegal port-number");
			port = Integer.parseInt(portNr);
			if (port < 1 || port > 65535)
				throw new URISyntaxException(url, "Port-number out of range");
		}
		
		if (includePort ^ port != -1) {
			final Integer defPort = DEFAULT_PORTS.get(protocol);
			if (includePort) {
				// no port value in input, try to get it from the default ports for the protocol
				if (defPort == null)
					throw new URISyntaxException(url, "no port number given and no default port for protocol '" + protocol + "'");
				port = defPort.intValue();
			} else {
				// don't make default port-number for the protocol part of the resulting url
				if (defPort != null && port == defPort.intValue())
					port = -1;
			}
		}
		
		final int hashmark = url.indexOf('#', (slashAfterHost == -1) ? hostEnd : slashAfterHost);
		if (slashAfterHost != -1) {
			// extract the path
			final int qmark = url.indexOf('?', slashAfterHost);
			final int pathEnd = (qmark == -1) ? (hashmark == -1) ? url.length() : hashmark : qmark;
			// XXX path = resolveBackpath(urlDecode(url.substring(slashAfterHost, pathEnd), charset));
			path = resolveBackpath(url.substring(slashAfterHost, pathEnd));
			if (appendSlash && qmark == -1 &&
					path.charAt(path.length() - 1) != '/' &&
					path.indexOf('.', path.lastIndexOf('/')) == -1) {
				path += '/';
			}
			
			// extract the query
			if (qmark != -1) {
				final int queryEnd = (hashmark == -1) ? url.length() : hashmark;
				if (queryEnd > qmark + 1) {
					final List<QueryEntry> queryList = new ArrayList<QueryEntry>();
					int paramStart = qmark + 1;
					do {
						int paramEnd = url.indexOf('&', paramStart);
						if (paramEnd == -1 || paramEnd > queryEnd)
							paramEnd = queryEnd;
						if (paramEnd > paramStart) {
							int eq = url.indexOf('=', paramStart);
							if (eq == -1 || eq > paramEnd)
								eq = paramEnd;
							
							try {
								String key = url.substring(paramStart, eq);
								try {
									// urlDecode(key.replace('+', ' '), charset);
									key = URLDecoder.decode(key, charset.name());
								} catch (IllegalArgumentException e) {
									// Illegal hex characters in escape (%) pattern
									logger.debug("error URL-decoding query-key '" + key + "': " + e.getMessage());
									// treat key as already replaced
								}
								
								String val = (eq < paramEnd) ? url.substring(eq + 1, paramEnd) : null;
								if (val != null) try {
									// urlDecode(val.replace('+', ' '), charset)
									val = URLDecoder.decode(val, charset.name());
								} catch (IllegalArgumentException e) {
									// Illegal hex characters in escape (%) pattern
									logger.debug("error URL-decoding query-value '" + val + "': " + e.getMessage());
									// treat val as already replaced
								}
								
								// System.out.println("query arg: " + key + " <-> " + val);
								queryList.add(new QueryEntry(key, val));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						paramStart = paramEnd + 1;
					} while (paramStart < queryEnd);
					if (sortQuery)
						Collections.sort(queryList);
					try {
						final StringBuilder sb = new StringBuilder(queryEnd - qmark);
						for (QueryEntry e : queryList) {
							sb.append(URLEncoder.encode(e.key, "UTF-8"));
							final String val = e.val;
							if (val != null)
								sb.append('=').append(URLEncoder.encode(val, "UTF-8"));
							sb.append('&');
						}
						sb.deleteCharAt(sb.length() - 1);
						query = sb.toString();
					} catch (UnsupportedEncodingException e) { e.printStackTrace(); }
				}
			}
			
			// extract the fragment
			if (hashmark != -1)
				// XXX fragment = urlDecode(url.substring(hashmark + 1), charset);
				// fragment = url.substring(hashmark + 1);
				;
		}
		
		// output
		final StringBuilder sb = new StringBuilder();
		sb.append(protocol).append("://");
		if (username != null) {
			sb.append(username);
			if (password != null)
				sb.append(':').append(password);
			sb.append('@');
		}
		sb.append(host);
		if (port != -1)
			sb.append(':').append(port);
		sb.append(path);
		if (query != null)
			sb.append('?').append(query);
		
		return new URI(sb.toString());
	}
	
	/**
	 * @see IFilter#filter(ICommand, IFilterContext)
	 */
	public void filter(ICommand command, IFilterContext filterContext) {
		final IParserDocument pdoc = command.getParserDocument();
		if (pdoc != null) {
			this.normalizeParserDoc(pdoc);
		}
	}
	
	/**
	 * Normalizes all {@link IParserDocument#getLinks() links} contained in a
	 * {@link IParserDocument parser-document}.
	 * 
	 * @param pdoc the {@link IParserDocument parser-document} 
	 */
	private void normalizeParserDoc(final IParserDocument pdoc) {
		/* =============================================================
		 * Normalize Sub-Documents
		 * ============================================================= */
		final Map<String,IParserDocument> subdocMap = pdoc.getSubDocs();
		if (subdocMap != null && subdocMap.size() > 0) {
			for (final IParserDocument subdoc : subdocMap.values()) {
				this.normalizeParserDoc(subdoc);
			}
		}
		
		/* =============================================================
		 * Normalize Links
		 * ============================================================= */		
		final Map<URI,LinkInfo> linkMap = pdoc.getLinks();
		if (linkMap == null || linkMap.size() == 0) return;
				
		final Map<URI,LinkInfo> normalizedLinks = new HashMap<URI,LinkInfo>();
		final Charset charset = (pdoc.getCharset() == null) ? UTF8 : pdoc.getCharset();		// UTF-8 is a recommended fallback but not standard yet
		
		final Iterator<Map.Entry<URI,LinkInfo>> it = linkMap.entrySet().iterator();
		while (it.hasNext()) {
			final Map.Entry<URI,LinkInfo> entry = it.next();
			final URI locationURI = entry.getKey();
			final String location = locationURI.toASCIIString();
			
			final URI normalizedURI = this.normalizeReference(location, charset);
			if (normalizedURI == null) {
				this.logger.info("removing malformed reference: " + location);
				// FIXME: shouldn't we call it.remove() here?
				continue;
			} else if (normalizedURI.equals(locationURI)) {
				continue;
			}
			
			this.logger.debug("normalized reference " + location + " to " + normalizedURI);
			normalizedLinks.put(normalizedURI, entry.getValue());
			it.remove();
		}
		linkMap.putAll(normalizedLinks);
	}
	
	private static final Pattern PATH_PATTERN = Pattern.compile("(/[^/]+(?<!/\\.{1,2})/)[.]{2}(?=/|$)|/\\.(?=/)|/(?=/)");
	
	/**
	 * Resolves backpaths
	 * @param path The path of an URL
	 * @return The path without backpath directives
	 */
	private static String resolveBackpath(String path) {
		
		if (path == null || path.length() == 0) return "/";
		if (path.length() == 0 || path.charAt(0) != '/') { path = "/" + path; }
		
		Matcher matcher = PATH_PATTERN.matcher(path);
		while (matcher.find()) {
			path = matcher.replaceAll("");
			matcher.reset(path);
		}
		
		return path.equals("")?"/":path;
	}
	
	static String urlDecode(final String str, final Charset charset) throws ParseException {
		int percent = str.indexOf('%');
		if (percent == -1)
			return str;
		
		final StringBuffer sb = new StringBuffer(str.length());				// buffer to build the converted string
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(8);	// buffer for conversion of contiguous %-encoded bytes
		int last = 0;
		final int len = str.length();
		do {
			sb.append(str.substring(last, percent));						// write non-encoded part
			
			// loop to convert sequence of %-encoded tokens into bytes. Contiguous byte-sequences have to be dealt with
			// in one block before decoding, because - dependant on the charset - more than one byte may be needed to
			// represent a single character. If the conversion to bytes was done sequentially, decoding might fail
			do {
				if (percent + 3 > str.length())
					throw new ParseException("unexpected end of input", percent + 3);
				final String token = str.substring(percent + 1, percent + 3);
				if (!token.matches("[0-9a-fA-F]{2}"))
					throw new ParseException("illegal url-encoded token '" + token + "'", percent);
				
				final int tokenValue = Integer.parseInt(token, 16) & 0xFF;
				baos.write(tokenValue);
				percent += 3;
			} while (percent < len && str.charAt(percent) == '%');
			
			if (baos.size() > 0) {
				final CharBuffer decoded = charset.decode(ByteBuffer.wrap(baos.toByteArray()));
				baos.reset();												// reuse the ByteArrayOutputStream in the next run
				for (int i=0; i<decoded.length(); i++) {
					final char c = decoded.charAt(i);
					switch (c) {
						case '#': sb.append("%23"); continue;
						case '%': sb.append("%25"); continue;
						case '&': sb.append("%26"); continue;
						case '=': sb.append("%3D"); continue;
						case '?': sb.append("%3F"); continue;
						default: sb.append(c); continue;
					}
				}
			}
			
			last = percent;													// byte after the token
			percent = str.indexOf('%', last);								// search for next token, returns -1 if last > len
		} while (percent != -1);
		return sb.append(str.substring(last)).toString();
	}
}
