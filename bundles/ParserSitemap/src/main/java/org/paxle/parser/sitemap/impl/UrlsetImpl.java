package org.paxle.parser.sitemap.impl;

import java.util.Iterator;

import javax.xml.stream.XMLStreamReader;

import org.paxle.parser.sitemap.api.Url;
import org.paxle.parser.sitemap.api.Urlset;

public class UrlsetImpl implements Urlset {

	private final XMLStreamReader parser;
	private UrlsetIteratorImpl iter;
	
	public UrlsetImpl(XMLStreamReader parser) {		
		if (parser == null) throw new NullPointerException("The stream-parser is null");
		this.parser = parser;
	}
	
	public Iterator<Url> iterator() {
		if (this.iter != null) return this.iter;
		this.iter = new UrlsetIteratorImpl(this.parser);		
		return this.iter;
	}

}
