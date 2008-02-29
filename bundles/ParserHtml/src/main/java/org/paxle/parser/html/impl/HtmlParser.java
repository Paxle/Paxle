
package org.paxle.parser.html.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.Parser;
import org.htmlparser.lexer.InputStreamSource;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.Source;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.html.IHtmlParser;

/**
 * Parses (X)HTML-pages using the html parser from
 * <a href="http://htmlparser.sf.net">http://htmlparser.sf.net</a>.
 * <p>
 *  It uses a kind of iterator with callback to walk through the node-tree of
 *  the HTML page, extracting information whereever supported and putting it
 *  into the {@link CachedParserDocument}.
 * </p>
 * @see org.htmlparser.Parser#visitAllNodesWith(org.htmlparser.visitors.NodeVisitor) for the iterator
 * @see org.paxle.parser.html.impl.NodeCollector for the callback
 */
public class HtmlParser implements IHtmlParser {
	
	private static final List<String> MIME_TYPES = Arrays.asList(
			"text/html",
			"application/xhtml+xml",
			"application/xml",
			"text/xml");
	
	private final Log logger = LogFactory.getLog(HtmlParser.class);
	
	// workaround for invalid relative links returned by Page due to BaseHrefTag setting the Page's
	// base URL to an empty String instead of null and therefore getAbsoluteURL() does not fall back
	// to getUrl() as it should when getBaseUrl() returns something invalid
	private static class FixedPage extends Page {
		
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
	}
	
	/*
	public static void main(String[] args) {
		final File dir = new File(args[0]);
		final String[] files = dir.list();
		for (int i=0; i<files.length; i++) try {
			ParserDocument doc = new ParserDocument();
			Page page = new PPage(new InputStreamSource(new FileInputStream(new File(dir, files[i]))));
			page.setUrl("http://www.example.com/");
			Parser parser = new Parser(new Lexer(page));
			System.out.println("PARSING: " + parser.getURL());
			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
			NodeCollector nc = new NodeCollector(doc);
			System.out.println(files[i]);
			parser.visitAllNodesWith(nc);
			page.close();
			System.out.println("-------------------------------------------------------------------------------------------");
			System.out.println();
			System.out.println(doc.toString());
		} catch (final Exception e) { e.printStackTrace(); }
	}*/
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	/**
	 * TODO: the html parser seems not to extract the keywords
	 */
	public IParserDocument parse(String location, String charset, File content) throws ParserException,
			UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			// testing if we support the charset. if not we try to use UTF-8
			if (charset != null && !Charset.isSupported(charset)) {
				this.logger.warn(String.format(
						"The resource '%s' has an unsupported charset '%s'. We try to use UTF-8 instead.", 
						location,
						charset
				));
				charset = "UTF-8";
			}
			
			final Page page = new FixedPage(new InputStreamSource(fis, charset));
			page.setUrl(location);
			final Parser parser = new Parser(new Lexer(page));
			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
			
			final IParserDocument doc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());
			final NodeCollector nc = new NodeCollector(doc, this.logger);
			parser.visitAllNodesWith(nc);
			page.close();
			
			if (charset != null)
				doc.setCharset(Charset.forName(charset));
			doc.setStatus(IParserDocument.Status.OK);
			return doc;
		} catch (org.htmlparser.util.ParserException e) {
			throw new ParserException("error parsing HTML nodes-tree", e);
		} finally {
			fis.close();
		}
	}
}
