
package org.paxle.parser.html.impl;

import java.net.MalformedURLException;
import java.net.URL;

import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.Source;

// workaround for invalid relative links returned by Page due to BaseHrefTag setting the Page's
// base URL to an empty String instead of null and therefore getAbsoluteURL() does not fall back
// to getUrl() as it should when getBaseUrl() returns something invalid
// 
public class FixedPage extends Page {
	
	private static final long serialVersionUID = 1L;
	
	public FixedPage(Source source) {
		super(source);
	}
	
	@Override
	public String getAbsoluteURL(String link, boolean strict) {
		String base;
		URL url;
		String ret;
		if ((null == link) || ("".equals(link))) {
			ret = "";
		} else {
			try {
				base = getBaseUrl();
				if (null == base || base.trim().length() == 0)
					base = getUrl();
				if (null == base) {
					ret = link;
				} else {
					url = constructUrl(link, base, strict);
					ret = url.toExternalForm();
				}
			} catch (MalformedURLException murle) {
				ret = link;
			}
		}
		return (ret);
	}
	
	@Override
	public URL constructUrl (String link, String base, boolean strict) throws MalformedURLException {
		String path;
		boolean modified;
		boolean absolute;
		int index;
		URL url; // constructed URL combining relative link and base
		
		// Bug #1461473 Relative links starting with ?
		if (!strict && ('?' == link.charAt (0))) {
			// remove query part of base if any
			if (-1 != (index = base.lastIndexOf ('?')))
				base = base.substring (0, index);
			url = new URL (base + link);
		} else
			url = new URL (new URL (base), link);
		path = url.getFile ();
		modified = false;
		absolute = link.startsWith ("/");
		if (!absolute) {
			// we prefer to fix incorrect relative links
			// this doesn't fix them all, just the ones at the start
			while (path.startsWith ("/.")) {
				if (path.startsWith ("/../")) {
					path = path.substring (3);
					modified = true;
				} else if (path.startsWith ("/./") /* FIXED: || path.startsWith("/.") */) {
					path = path.substring (2);
					modified = true;
				} else
					break;
			}
		}
		// fix backslashes
		while (-1 != (index = path.indexOf ("/\\"))) {
			path = path.substring (0, index + 1) + path.substring (index + 2);
			modified = true;
		}
		if (modified)
			url = new URL (url, path);
		
		return (url);
	}
}