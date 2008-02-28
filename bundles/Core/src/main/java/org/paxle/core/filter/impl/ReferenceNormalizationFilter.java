package org.paxle.core.filter.impl;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class ReferenceNormalizationFilter implements IFilter {

	public void filter(ICommand command, IFilterContext filterContext) {
	}

	/**
	 * This method takes an URL as input and returns it in a normalized form. There should be no change in the functionality of the URL.
	 * @param location the unnormalized URL
	 * @return the normalized URL
	 * @author Roland Ramthun, Franz Brauﬂe
	 * @throws MalformedURLException 
	 */
	public static String normalizeLocation(String location) throws MalformedURLException {

		//We can issue this warning, as the HTTP/HTML processing parts (i.e. crawler and parser) should give us the clear text
		//So how could an unresolved encoding get here?
		if (location.contains("%")) {
			System.err.println("URL '" + location + "' contains encoded reserved characters, which should not be. Please file a bug-report.");
		}

		OwnURL locurl = null;
		locurl = new OwnURL(location);

		return locurl.toNormalizedString();
	}

	/**
	 * Resolves backpaths
	 * @param path The path of an URL
	 * @return The path without backpath directives
	 */
	private static String resolveBackpath(String path) {

		final Pattern PATH_PATTERN = Pattern.compile("(/[^/]+(?<!/\\.{1,2})/)[.]{2}(?=/|$)|/\\.(?=/)|/(?=/)");

		if (path == null || path.length() == 0) return "/";
		if (path.length() == 0 || path.charAt(0) != '/') { path = "/" + path; }

		Matcher matcher = PATH_PATTERN.matcher(path);
		while (matcher.find()) {
			path = matcher.replaceAll("");
			matcher.reset(path);
		}

		return path.equals("")?"/":path;
	}

static class OwnURL {
		
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
	 * 
	 */
	
		private static final HashMap<String,Integer> DEFAULT_PORTS = new HashMap<String,Integer>();

		static {
			DEFAULT_PORTS.put("ftp", Integer.valueOf(21));
			DEFAULT_PORTS.put("http", Integer.valueOf(80));
			DEFAULT_PORTS.put("https", Integer.valueOf(443));
		}
		
		private String protocol;
		private String username;
		private String password;
		private String host;
		private int port = -1;
		private String path = "/";
		private LinkedHashMap<String,String> query;
		private String fragment;
		
		public OwnURL(String url) throws MalformedURLException {
			parseBaseUrlString(url);
		}
		
		private void parseBaseUrlString(final String url) throws MalformedURLException {
			// extract the protocol
			final int colonpos = url.indexOf(':');
			if (colonpos <= 0)
				throw new MalformedURLException("No protocol specified in URL " + url);
			protocol = url.substring(0, colonpos).toLowerCase();
			
			final int protocolEnd;
			if (url.charAt(colonpos + 1) == '/' && url.charAt(colonpos + 2) == '/') {
				protocolEnd = colonpos + 3;
			} else {
				throw new MalformedURLException("No valid protocol identifier given in URL " + url);
			}
			
			// extract username / password
			final int slashAfterHost = url.indexOf('/', protocolEnd);
			final int at = url.indexOf('@');
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
			final int hostEnd = (portColon == -1) ? (slashAfterHost == -1) ? url.length() : slashAfterHost : portColon;
			host = url.substring(hostStart, hostEnd).toLowerCase();
			
			// TODO: de-punycode host
			if (host.contains("--")) {
				throw new MalformedURLException(url + " is a punycode domain. No support yet.");
			}
			
			// extract the port
			final int portEnd = (slashAfterHost == -1) ? url.length() : slashAfterHost;
			if (portColon != -1 && portColon < portEnd) {
				final String portNr = url.substring(portColon + 1, portEnd);
				if (!portNr.matches("\\d{1,5}"))
					throw new MalformedURLException("Illegal port-number in URL " + url);
				port = Integer.parseInt(portNr);
				if (port < 1 || port > 65535)
					throw new MalformedURLException("Port-number out of range in URL " + url);
				final Integer defPort = DEFAULT_PORTS.get(protocol);
				if (defPort != null && port == defPort.intValue())
					port = -1;
			}
			
			if (slashAfterHost == -1)
				return;
			
			// extract the path
			final int qmark = url.indexOf('?', slashAfterHost);
			final int hashmark = url.indexOf('#', slashAfterHost);
			final int pathEnd = (qmark == -1) ? (hashmark == -1) ? url.length() : hashmark : qmark;
			path = resolveBackpath(url.substring(slashAfterHost, pathEnd));
			
			// extract the query
			if (qmark != -1) {
				final int queryEnd = (hashmark == -1) ? url.length() : hashmark;
				if (queryEnd > qmark + 1) {
					this.query = new LinkedHashMap<String,String>();
					int paramStart = qmark + 1;
					do {
						int paramEnd = url.indexOf('&', paramStart);
						if (paramEnd == -1 || paramEnd > queryEnd)
							paramEnd = queryEnd;
						final int eq = url.indexOf('=', paramStart);
						if (eq == -1 || eq > paramEnd)
							throw new MalformedURLException("Illegal query parameter " + url.substring(paramStart, paramEnd) + " in URL " + url);
						this.query.put(url.substring(paramStart, eq), url.substring(eq + 1, paramEnd));
						paramStart = paramEnd + 1;
					} while (paramStart < queryEnd);
				}
			}
			
			// extract the fragment
			if (hashmark != -1)
				fragment = url.substring(hashmark + 1);
		}
		
		private StringBuffer appendQuery(final StringBuffer sb) {
			for (Map.Entry<String,String> e : query.entrySet())
				sb.append(e.getKey()).append('=').append(e.getValue()).append('&');
			sb.deleteCharAt(sb.length() - 1);
			return sb;
		}
		
		/**
		 * Assembles the normalized representation of the given URL
		 * @return the normalized URL
		 */
		public String toNormalizedString() {
			final StringBuffer sb = new StringBuffer(protocol).append("://");
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
				appendQuery(sb.append('?'));
			return sb.toString();
		}
	}
	
}


