
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
			final NodeCollector nc = new NodeCollector(doc, NodeCollector.Debug.LOW);
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
