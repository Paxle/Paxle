package org.paxle.core.filter.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class ReferenceNormalizationFilter implements IFilter {

	public void filter(ICommand command, IFilterContext filterContext) {
		// TODO Auto-generated method stub
	
	}
	
	/**
	 * This method takes an URL as input and returns it in a normalized form. There should be no change in the functionality of the URL.
	 * This function is not speed optimized yet, as safety is more important than speed.
	 * @param location the unnormalized URL
	 * @return the normalized URL
	 * @author Roland Ramthun
	 */
	public static String normalizeLocation(String location) {
		
		System.out.println("Started normalization of URL: '" + location + "'");
		
		String newloc = location;
		
		/*
		 * Description: Add trailing slash to URL
		 * 
		 * Assumptions:
		 * 1. A directory has to be denoted with a trailing slash (e.g. "http://paxle.net/pictures/")
		 * 2. Every URL has to end with a slash, if it only consists of a hostname (this is in fact a directory on a server).
		 * 3. If the string contains no single slash ("/"), there must be added the trailing slash.
		 * 
		 * Requirements:
		 * 1. This step doesn't require any prior work on the string
		 * 
		 * Example:
		 * "http://paxle.net"-->"http://paxle.net/"
		 */
		
		if (!(location.lastIndexOf("/") > location.lastIndexOf("//") + 1)) {
			System.out.print("'" + newloc + "'" + " --> '");
			newloc += "/";
			System.out.println(newloc + "'");
		}
		
		/*
		 * Description: Remove unnecessary ports from URL
		 * These are:
		 * 80 for HTTP
		 * 443 for HTTPS
		 * 21 for FTP
		 * 22 for Telnet
		 * 23 for SSH
		 * 
		 * Assumptions: 
		 * 1. The port is the first number in the string
		 * 2. The port has a leading substring ":"
		 * 3. The port is followed by a slash
		 * 4. "Add trailing slash" has to be done before this step, as assumption 3 would be wrong otherwise
		 * 
		 *  Example:
		 *  "http://paxle.net:80/download.htm" --> "http://paxle.net/download.htm"
		 */
		
		//TO-DO: Check port range and check for trailing slash
		//newloc = newloc.replaceFirst(":[0-9]{1,5}", "");
		
		/*
		 * Convert scheme and hostname to lowercase
		 */
		
		/*
		 * Capitalize letters in escape sequences
		 */
		
		/*
		 * Description: Resolve backpaths
		 * 
		 *  Example:
		 *  "http://example.org/test/.././x.html" --> "http://example.org/x.html"
		 *  "http://example.org/test/.././x/../" --> "http://example.org/"
		 */
		
		//WARNING: Breaks normalization. See #29
		//newloc = resolveBackpath(newloc);
		
		/*
		 * Reorder GET-parameters alphabetically: http://paxle.net/content?lang=en&article=URL --> http://www.paxle.net/content?article=URL&lang=en
		 */
		
		/*
		 * Remove the fragment.
		 */
		
		System.out.println("Finished normalization of URL: '" + location + "' --> '" + newloc + "'");
		
		return newloc;
	}
	
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
