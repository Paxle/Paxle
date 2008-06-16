package org.paxle.parser.sitemap.impl;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.paxle.parser.sitemap.api.Url;
import org.paxle.parser.sitemap.api.Url.ChangeFrequency;

public class UrlsetIteratorImpl implements Iterator<Url> {
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private final XMLStreamReader parser;
	private SimpleDateFormat lastModFormater = new SimpleDateFormat(DATE_FORMAT);

	public UrlsetIteratorImpl(XMLStreamReader parser) {
		if (parser == null) throw new NullPointerException("The stream-parser is null");
		this.parser = parser;
	}

	public boolean hasNext() {
		try {
			return this.parser.hasNext();
		} catch (XMLStreamException e) {
			throw new RuntimeException(e);
		}
	}

	public Url next() {
		try {
			int event = -1;

			while (true) {
				// read the start of the url-element 
				event = this.parser.next();
				if (event == XMLStreamConstants.END_DOCUMENT) {
					this.parser.close();
					return null;
				} else if (event != XMLStreamConstants.START_ELEMENT) {
					continue;
				}
				break;
			}

			// reading all sub-elements
			StringBuilder buf = new StringBuilder();
			UrlImpl url = new UrlImpl();

			while(true) {
				event = this.parser.next();
				switch (event) {
					case XMLStreamConstants.END_DOCUMENT:
						this.parser.close();
						return null;

					case XMLStreamConstants.CHARACTERS:
						int start = this.parser.getTextStart();
				        int length = this.parser.getTextLength();
				        buf.append(this.parser.getTextCharacters(),start,length);
				        break;
				        
					case XMLStreamConstants.END_ELEMENT:
						String name = this.parser.getLocalName().toLowerCase();
						String value = buf.toString().trim();

						if (name.equals("url")) {
							return url;
						} else if (name.equals("loc")) {
							url.setLocation(URI.create(value));
						} else if (name.equals("lastmod")) {
							if (value.length() > DATE_FORMAT.length()) {
								value = value.substring(0,DATE_FORMAT.length());
							}
							Date lastMod = this.lastModFormater.parse(value);
							url.setLastMod(lastMod);
						} else if (name.equals("changefreq")) {
							ChangeFrequency freq = Url.ChangeFrequency.valueOf(value);
							url.setChangeFreq(freq);
						} else if (name.equals("priority")) {
							Float prio = Float.valueOf(value);
							url.setPriority(prio);
						}
						
						buf.setLength(0);
						break;
					default:
						break;
				}
			}
		} catch (Exception e) {
			try { this.parser.close(); } catch (Exception ex) {/* ignore this */}
			throw new RuntimeException(e);
		}
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

}
