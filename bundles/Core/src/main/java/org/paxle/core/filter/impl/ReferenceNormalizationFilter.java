package org.paxle.core.filter.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class ReferenceNormalizationFilter implements IFilter {

	private static final int PORT = 1;
	private static final int PROTOCOL = 2;
	private static final int HOST = 3;
	private static final int QUERY = 4;
	private static final int PATH = 5;
	private static final int USERINFO = 6;

	public void filter(ICommand command, IFilterContext filterContext) {
		// TODO Auto-generated method stub

	}

	/**
	 * This method takes an URL as input and returns it in a normalized form. There should be no change in the functionality of the URL.
	 * @param location the unnormalized URL
	 * @return the normalized URL
	 * @author Roland Ramthun
	 */
	public static String normalizeLocation(String location) {

		/*
		 * What will we do and how?
		 * 
		 * First of all we create a standard Java URL object, which automatically checks the URL for correct syntax.
		 * Then we put the key parts of this URL object in a Hashmap to work on them later.
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
		 * As last step we reorder GET parameters alphabetically.
		 * 
		 * Then the resulting normalized URL is assembled, along this way possible fragments are removed, as they are simply not added
		 * 
		 */

		//We can issue this warning, as the HTML processing parts should work on the UTF-8 string and must therefore transform the URL for themselves.
		//So how could an unresolved encoding get here?
		if (location.contains("%")) {
			System.err.println("URL '" + location + "' contains encoded reserved characters, which should not be. Please file a bug-report.");
		}

		URL locurl = null;
		try {
			locurl = new URL(location);
		} catch (MalformedURLException e) {
			System.err.println("URL '" + location + "' is malformed.");
			return null;
		}

		HashMap<Integer, String> url = new HashMap<Integer, String>();

		//user:pw
		url.put(USERINFO, locurl.getUserInfo());
		//domain incl. subdomain
		url.put(HOST, locurl.getHost());
		//all parameters after the ?
		url.put(QUERY, locurl.getQuery());
		//the path and the document, e.g. "/path/x.htm"
		url.put(PATH, locurl.getPath());
		//returns the protocol without trailing ://
		url.put(PROTOCOL, locurl.getProtocol());
		//the given port, -1 if none given
		url.put(PORT, "" + locurl.getPort());

		// Java always strips the slash from the host, so we have to add it to the path in every case
		if (url.get(PATH) == null) {
			url.put(PATH, "/");
		}

		/*
		 * Empty parameter list sets the query to "". We want it to be null, to remove the question mark from the URL while final assembly.
		 * 
		 * Example:
		 * "http://example.org/x.php?" --> "http://example.org/x.php"
		 */
		
		if (url.get(QUERY) != null && url.get(QUERY).equals("")) {
			url.put(QUERY, null);
		}
		
		/*
		 * Remove unnecessary default ports from URL
		 */
		if ((url.get(PORT).equals(String.valueOf(locurl.getDefaultPort()))) || url.get(PORT).equals("-1")) {
			url.put(PORT, "");
		} else {
			url.put(PORT, ":" + url.get(PORT));
		}

		/*
		 * Convert scheme and hostname to lowercase
		 */
		url.put(PROTOCOL, url.get(PROTOCOL).toLowerCase());
		url.put(HOST, url.get(HOST).toLowerCase());

		/*
		 * Resolve backpaths
		 * 
		 *  Example:
		 *  "http://example.org/test/.././x.html" --> "http://example.org/x.html"
		 *  "http://example.org/test/.././x/../" --> "http://example.org/"
		 */

		url.put(PATH, resolveBackpath(url.get(PATH)));

		//Final URL assembly:
		// protocol + :// + userinfo + host + port + path + query
		StringBuffer newloc = new StringBuffer(20);
		newloc.append(url.get(PROTOCOL));
		newloc.append("://");
		if (url.get(USERINFO) != null) {
			newloc.append(url.get(USERINFO) + "@");
		}
		newloc.append(url.get(HOST));
		newloc.append(url.get(PORT));
		newloc.append(url.get(PATH));
		if (url.get(QUERY) != null) {
			newloc.append("?" + url.get(QUERY));
		}

		System.out.println("Normalization of URL: '" + location + "' --> '" + newloc.toString() + "'");

		return newloc.toString();
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

}
