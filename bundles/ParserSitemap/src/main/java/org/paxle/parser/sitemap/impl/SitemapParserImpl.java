package org.paxle.parser.sitemap.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.paxle.parser.sitemap.SitemapParser;
import org.paxle.parser.sitemap.api.Urlset;

public class SitemapParserImpl implements SitemapParser {
	public Urlset getUrlSet(File dataFile) throws IOException, XMLStreamException {
		InputStream in = null;
		FileInputStream fileIn = new FileInputStream(dataFile);
		BufferedInputStream buf = new BufferedInputStream(fileIn);
		buf.mark(2);
		char c1 = (char) buf.read();
		char c2 = (char) buf.read();
		buf.reset();
		
		if (c1 == '\037' && c2 == '\213') {
			in = new GZIPInputStream(buf);
		} else {
			in = buf;
		}
		
		XMLInputFactory factory = XMLInputFactory.newInstance();
		XMLStreamReader parser = factory.createXMLStreamReader(in);
		
		return new UrlsetImpl(parser);
	}
}
