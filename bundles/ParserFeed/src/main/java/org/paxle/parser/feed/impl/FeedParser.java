/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.parser.feed.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.parser.ASubParser;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.paxle.parser.feed.IFeedParser;
import org.xml.sax.InputSource;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ImageIF;
import de.nava.informa.core.ParseException;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;

public class FeedParser extends ASubParser implements IFeedParser {
	
	private static final List<String> MIMETYPES = Arrays.asList(
			"text/rss",
			"application/rdf+xml",
			"application/rss+xml",
			"application/atom+xml");
	
	private final Log logger = LogFactory.getLog(FeedParser.class);
	
	public List<String> getMimeTypes() {
		return MIMETYPES;
	}
	
	@Override
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {				
		final ParserContext context = ParserContext.getCurrentContext();
		
		// the result object
		final IParserDocument pdoc = new CachedParserDocument(context.getTempFileManager());		
		try {
			// parse the feed
			final ChannelBuilder builder = new ChannelBuilder();
			final ChannelIF channel = de.nava.informa.parsers.FeedParser.parse(builder, new InputSource(is), location.toURL());
			
			// extracting title/summary/language
			pdoc.setTitle(channel.getTitle());
			pdoc.setSummary(channel.getDescription());
			final String language = channel.getLanguage();
			if (language != null)
				pdoc.setLanguages(new HashSet<String>(Arrays.asList(channel.getLanguage().split(","))));
			
			final ImageIF image = channel.getImage();
			if (image != null) try {
				pdoc.addImage(image.getLink().toURI(), image.getTitle());
			} catch (URISyntaxException e) { 
				logger.warn("image-link is not valid: '" + image.getLink() + "': " + e.getMessage()); 
			}
			
			final Collection<?> items = channel.getItems();
			if (!items.isEmpty()) {
				final StringBuilder authors = new StringBuilder();
				
				// loop through all items
				final Iterator<?> it = items.iterator();				
				while (it.hasNext()) {
					final Item item = (Item)it.next();
					
					final String itemTitle = item.getTitle();
					final String itemDescr = item.getDescription();
					final String itemCreator = item.getCreator();
					
					URI   itemURL   = null;
					try {
						itemURL = new URI(item.getLink().toExternalForm());
					} catch (URISyntaxException e) { 
						logger.warn("item-link is not valid: '" + itemURL + "': " + e.getMessage()); 
					}
					
					IParserDocument idoc = null;
					String itemContent = item.getElementValue("content");
					if (itemContent == null || itemContent.length() == 0)
						itemContent = item.getElementValue("content:encoded");
					if ((itemContent != null) && (itemContent.length() > 0)) {						
						final URI itemLocation = (itemURL == null) ? location : itemURL;
						
						// parsing the html content
						final ISubParser htmlParser = context.getParser("text/html");
						if (htmlParser != null) {
							try {
								idoc = htmlParser.parse(
										itemLocation, 
										charset,
										new ByteArrayInputStream(itemContent.getBytes())
								);							
							} catch (Exception e) { 
								logger.warn("error parsing feed-item '" + itemLocation + "', ignoring", e); 
							}
						}
					}
					if (idoc == null)
						idoc = new ParserDocument();
					
					if (itemCreator != null && itemCreator.length() > 0)
						authors.append(",").append(itemCreator);
					
					idoc.addHeadline(itemTitle);
					
					if (itemURL != null)
						idoc.addReference(itemURL, itemTitle);
					
					if (itemDescr != null)
						idoc.addText(itemDescr);
					
					pdoc.addSubDocument(itemTitle, idoc);
				}
			}
			
			pdoc.setStatus(IParserDocument.Status.OK);
			return pdoc;
		} catch (ParseException e) {
			throw new ParserException("error parsing feed", e);
		}
	}
}
