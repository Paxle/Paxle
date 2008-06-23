package org.paxle.parser.sitemap.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.paxle.core.doc.IParserDocument;
import org.paxle.parser.ASubParser;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.sitemap.SitemapParser;
import org.paxle.parser.sitemap.api.Url;
import org.paxle.parser.sitemap.api.Urlset;

public class SitemapParserImpl extends ASubParser implements SitemapParser {
	private static final List<String> MIME_TYPES = Arrays.asList(
			// this is no official mimetype but we need one to register this parser to the manager
			"application/sitemap+xml"
	);
	
	/**
	 * @see SitemapParser#getUrlSet(File)
	 */
	public Urlset getUrlSet(File dataFile) throws IOException, XMLStreamException {
		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream(dataFile);
			return this.getUrlSet(fileIn);
		} finally {
			// we are streaming the xml on the fly. we are not allowed to close the stream!
			// if (fileIn != null) try  { fileIn.close(); } catch (Exception e) {/* ignore this */}
		}
	}
	
	public Urlset getUrlSet(InputStream dataStream) throws IOException, XMLStreamException {
		// we need to pre-read data
		if (!dataStream.markSupported()) {
			dataStream = new BufferedInputStream(dataStream);
		}
		
		// read the first two chars to determine if data is gzip compressed
		dataStream.mark(2);
		char c1 = (char) dataStream.read();
		char c2 = (char) dataStream.read();
		dataStream.reset();
		
		if (c1 == '\037' && c2 == '\213') {
			dataStream = new GZIPInputStream(dataStream);
		} 
		
		// creat a new stax parser
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(dataStream);
		
		return new UrlsetImpl(parser);		
	}

	/**
	 * @see ISubParser#getMimeTypes()
	 */
	public List<String> getMimeTypes() {
		return MIME_TYPES;
	}

	/**
	 * @see ISubParser#parse(URI, String, InputStream)
	 */
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {
		try {
			// creating an empty parser document 
			final IParserDocument pdoc = new CachedParserDocument(ParserContext.getCurrentContext().getTempFileManager());
			// TODO: we need a way to mark the document as "parsing-only" and to disallow indexing
			
			Urlset urls = this.getUrlSet(is);
			if (urls != null) {
				for (Url url : urls) {
					if (url == null) break;
					// TODO: we could extract more metadata for each url here
					pdoc.addReference(url.getLocation(), url.getLocation().toASCIIString());
				}				
			}
			
			pdoc.setStatus(IParserDocument.Status.OK);
			return pdoc;
		} catch (Throwable e) {
			throw new ParserException("Unable to parse the sitemap document", e);
		}			
	}
}