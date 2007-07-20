
package org.paxle.parser.html.impl;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.lexer.InputStreamSource;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.Source;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ParserDocument;
import org.paxle.parser.ParserTools;
import org.paxle.parser.ParserException;
import org.paxle.parser.html.IHtmlParser;

/**
 * Parses (X)HTML-pages using the html parser from
 * <a href="http://htmlparser.sf.net">http://htmlparser.sf.net</a>.
 * <p>
 *  It uses a kind of iterator with callback to walk through the node-tree of
 *  the HTML page, extracting information whereever supported and putting it
 *  into the {@link ParserDocument}.
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
	
	public IParserDocument parse(String location, String charset, File content) throws ParserException {
		try {
			final Source source = new InputStreamSource(ParserTools.getInputStream(content));
			if (charset != null)
				source.setEncoding(charset);
			final Page page = new Page(source);
			page.setUrl(location);
			final Parser parser = new Parser(new Lexer(page));
			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
			
			final IParserDocument doc = new ParserDocument();
			final NodeCollector nc = new NodeCollector(doc, NodeCollector.Debug.NONE);
			parser.visitAllNodesWith(nc);
			source.close();
			return doc;
		} catch (final RuntimeException e) {
			throw e;
		} catch (final Exception e) {
			throw new ParserException("error parsing document " + location, e);
		}
	}
}
