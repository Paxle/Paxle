/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.html.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.PageIndex;
import org.htmlparser.lexer.Source;

// workaround for invalid relative links returned by Page due to BaseHrefTag setting the Page's
// base URL to an empty String instead of null and therefore getAbsoluteURL() does not fall back
// to getUrl() as it should when getBaseUrl() returns something invalid
// 
public class FixedPage extends Page {
	
	private static final long serialVersionUID = 1L;
	
	private final ParserLogger logger;
	
	public FixedPage(Source source, final ParserLogger logger) {
		super(source);
		this.logger = logger;
	}
	
	public FixedPage(final ParserLogger logger) {
		this.logger = logger;
	}
	
	public void init(final Source source) {
        mSource = source;
        mIndex = new PageIndex(this);
        mConnection = null;
        mUrl = null;
        mBaseUrl = null;
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

    /**
     * Get a CharacterSet name corresponding to a charset parameter.
     * @param content A text line of the form:
     * <pre>
     * text/html; charset=Shift_JIS
     * </pre>
     * which is applicable both to the HTTP header field Content-Type and
     * the meta tag http-equiv="Content-Type".
     * Note this method also handles non-compliant quoted charset directives
     * such as:
     * <pre>
     * text/html; charset="UTF-8"
     * </pre>
     * and
     * <pre>
     * text/html; charset='UTF-8'
     * </pre>
     * @return The character set name to use when reading the input stream.
     * For JDKs that have the Charset class this is qualified by passing
     * the name to findCharset() to render it into canonical form.
     * If the charset parameter is not found in the given string, the default
     * character set is returned.
     * @see #findCharset
     * @see #DEFAULT_CHARSET
     */
	@Override	// to suppress logging of fallback-charset to System.out
    public String getCharset (String content)
    {
        final String CHARSET_STRING = "charset";
        int index;
        String ret;

        if (null == mSource)
            ret = DEFAULT_CHARSET;
        else
            // use existing (possibly supplied) character set:
            // bug #1322686 when illegal charset specified
            ret = mSource.getEncoding ();
        if (null != content)
        {
            index = content.indexOf (CHARSET_STRING);

            if (index != -1)
            {
                content = content.substring (index +
                    CHARSET_STRING.length ()).trim ();
                if (content.startsWith ("="))
                {
                    content = content.substring (1).trim ();
                    index = content.indexOf (";");
                    if (index != -1)
                        content = content.substring (0, index);

                    //remove any double quotes from around charset string
                    if (content.startsWith ("\"") && content.endsWith ("\"")
                        && (1 < content.length ()))
                        content = content.substring (1, content.length () - 1);

                    //remove any single quote from around charset string
                    if (content.startsWith ("'") && content.endsWith ("'")
                        && (1 < content.length ()))
                        content = content.substring (1, content.length () - 1);

                    ret = findCharset (content, ret, logger);

                    // Charset names are not case-sensitive;
                    // that is, case is always ignored when comparing
                    // charset names.
//                    if (!ret.equalsIgnoreCase (content))
//                    {
//                        System.out.println (
//                            "detected charset \""
//                            + content
//                            + "\", using \""
//                            + ret
//                            + "\"");
//                    }
                }
            }
        }

        return (ret);
    }

    /**
     * Lookup a character set name.
     * <em>Vacuous for JVM's without <code>java.nio.charset</code>.</em>
     * This uses reflection so the code will still run under prior JDK's but
     * in that case the default is always returned.
     * @param name The name to look up. One of the aliases for a character set.
     * @param fallback The name to return if the lookup fails.
     * @return The character set name.
     */
    public static String findCharset (String name, String fallback, final ParserLogger logger)
    {
        String ret;

        try
        {
            Class<?> cls;
            Method method;
            Object object;

            cls = Class.forName ("java.nio.charset.Charset");
            method = cls.getMethod ("forName", new Class[] { String.class });
            object = method.invoke (null, new Object[] { name });
            method = cls.getMethod ("name", new Class[] { });
            object = method.invoke (object, new Object[] { });
            ret = (String)object;
        }
        catch (ClassNotFoundException cnfe)
        {
            // for reflection exceptions, assume the name is correct
            ret = name;
        }
        catch (NoSuchMethodException nsme)
        {
            // for reflection exceptions, assume the name is correct
            ret = name;
        }
        catch (IllegalAccessException ia)
        {
            // for reflection exceptions, assume the name is correct
            ret = name;
        }
        catch (InvocationTargetException ita)
        {
            // java.nio.charset.IllegalCharsetNameException
            // and java.nio.charset.UnsupportedCharsetException
            // return the default
            ret = fallback;
            logger.logInfo(
                "unable to determine cannonical charset name for "
                + name
                + " - using "
                + fallback);
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