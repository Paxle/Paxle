package org.paxle.parser.sitemap;

import java.io.File;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.paxle.parser.ISubParser;
import org.paxle.parser.sitemap.api.Urlset;

public interface SitemapParser {
	public Urlset getUrlSet(File dataFile) throws IOException, XMLStreamException;
}
