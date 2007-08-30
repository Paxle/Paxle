
package org.paxle.parser.html.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
			"text/xml");
	
	private final Log logger = LogFactory.getLog(HtmlParser.class);
	
	public static void main(String[] args) {
		final HtmlParser p = new HtmlParser();
		final File dir = new File(args[0]);
		final String[] files = dir.list();
		for (int i=0; i<files.length; i++) try {
			System.out.println(p.parse(files[i], null, new File(dir, files[i])).toString());
		} catch (final Exception e) { e.printStackTrace(); }
	}
	
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
			final Page page = new Page(new InputStreamSource(fis, charset));
			page.setUrl(location);
			final Parser parser = new Parser(new Lexer(page));
			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
			
			final IParserDocument doc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());
			final NodeCollector nc = new NodeCollector(doc, this.logger);
			parser.visitAllNodesWith(nc);
			page.close();
			
			return doc;
		} catch (org.htmlparser.util.ParserException e) {
			throw new ParserException("error parsing HTML nodes-tree", e);
		} finally {
			fis.close();
		}
	}
}
