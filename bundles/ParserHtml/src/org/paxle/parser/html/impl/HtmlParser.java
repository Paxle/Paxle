
package org.paxle.parser.html.impl;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;

import org.htmlparser.Parser;
import org.htmlparser.lexer.InputStreamSource;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.lexer.Source;

import org.paxle.parser.ParserDocument;
import org.paxle.parser.ParserTools;
import org.paxle.parser.ParserException;
import org.paxle.parser.html.IHtmlParser;

class NullWriter extends Writer {
	@Override public void close() throws IOException {  }
	@Override public void flush() throws IOException {  }
	@Override public void write(char[] cbuf, int off, int len) throws IOException {  }
}

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
		/*
		int count = (args.length > 1) ? Integer.parseInt(args[1]) : 1;
		for (int c=0; c<count; c++)
		*/
			for (int i=0; i<files.length; i++) try {
				// final Command cmd = new Command(is, new NullWriter(), files[i]);
				p.parse(files[i], null, new File(dir, files[i]));
				//final PrintWriter w = new PrintWriter(new FileWriter(new File(files[i] + ".out"))); 
				//cmd.print(w);
				//w.close();
			} catch (final Exception e) { e.printStackTrace(); }
	}
	
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}
	
	public ParserDocument parse(String location, String charset, File content) throws ParserException {
		try {
			final Source source = new InputStreamSource(ParserTools.getInputStream(content));
			if (charset != null)
				source.setEncoding(charset);
			final Page page = new Page(source);
			page.setUrl(location);
			final Parser parser = new Parser(new Lexer(page));
			parser.setNodeFactory(NodeCollector.NODE_FACTORY);
			
			final ParserDocument doc = new ParserDocument(location);
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
