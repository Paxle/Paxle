
package org.paxle.parser.html.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.Parser;
import org.htmlparser.lexer.InputStreamSource;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;

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
			"text/xml",
			"text/sgml");
	
	private final Log logger = LogFactory.getLog(HtmlParser.class);
	
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
	
	public IParserDocument parse(URI location, String charset, InputStream is)
			throws ParserException, UnsupportedEncodingException, IOException {
		try {
			// testing if we support the charset. if not we try to use UTF-8
			boolean unsupportedCharset = false;
			try {
				if (charset != null && !Charset.isSupported(charset)) {
					unsupportedCharset = true;
				}
			} catch (IllegalCharsetNameException e) {
				unsupportedCharset = true;
			}
			
			if (unsupportedCharset) {
				this.logger.warn(String.format(
						"The resource '%s' has an unsupported charset '%s'. We try to use UTF-8 instead.", 
						location,
						charset
				));
				charset = "UTF-8";
			}
			
			final Page page = new FixedPage(new InputStreamSource(is, charset));
			page.setUrl(location.toASCIIString());
			final Parser parser = new Parser(new Lexer(page));
			parser.setNodeFactory(NodeCollector.NODE_FACTORY);

			final ParserContext context = ParserContext.getCurrentContext();
			final IParserDocument doc = new CachedParserDocument(context.getTempFileManager());
			final NodeCollector nc = new NodeCollector(doc, new ParserLogger(logger, location), page, context.getReferenceNormalizer());
			parser.visitAllNodesWith(nc);
			page.close();
			
			if (charset != null && doc.getCharset() == null)
				doc.setCharset(Charset.forName(charset));
			doc.setStatus(IParserDocument.Status.OK);
			return doc;
		} catch (org.htmlparser.util.ParserException e) {
			throw new ParserException("error parsing HTML nodes-tree", e);
		}
	}
	
	/**
	 * TODO: the html parser does not seem to extract the keywords
	 */
	public IParserDocument parse(URI location, String charset, File content) throws ParserException,
			UnsupportedEncodingException, IOException {
		final FileInputStream fis = new FileInputStream(content);
		try {
			return parse(location, charset, fis);
		} finally {
			fis.close();
		}
	}
}
