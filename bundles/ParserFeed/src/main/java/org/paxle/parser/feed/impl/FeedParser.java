
package org.paxle.parser.feed.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
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
	public IParserDocument parse(URI location, String charset, InputStream is)
	throws ParserException, UnsupportedEncodingException, IOException {
		
		final ChannelBuilder builder = new ChannelBuilder();
		final ParserContext context = ParserContext.getCurrentContext();
		final IParserDocument pdoc = new CachedParserDocument(context.getTempFileManager());
		
		try {
			final ChannelIF channel = de.nava.informa.parsers.FeedParser.parse(builder, is);
			pdoc.setTitle(channel.getTitle());
			pdoc.setSummary(channel.getDescription());
			
			final ImageIF image = channel.getImage();
			if (image != null) try {
				pdoc.addImage(image.getLink().toURI(), image.getTitle());
			} catch (URISyntaxException e) { logger.warn("image-link is not valid: '" + image.getLink() + "': " + e.getMessage()); }
			
			final Collection<?> items = channel.getItems();
			if (!items.isEmpty()) {
				final Iterator<?> it = items.iterator();
				final StringBuilder authors = new StringBuilder();
				while (it.hasNext()) {
					final Item item = (Item)it.next();
					
					final String itemTitle = item.getTitle();
					URI    itemURL   = null;
					try {
						itemURL = new URI(item.getLink().toExternalForm());
					} catch (URISyntaxException e) { logger.warn("item-link is not valid: '" + itemURL + "': " + e.getMessage()); }
					final String itemDescr = item.getDescription();
					final String itemCreator = item.getCreator();
					
					IParserDocument idoc = null;
					final String itemContent = item.getElementValue("content");
					if ((itemContent != null) && (itemContent.length() > 0)) {
						final ISubParser htmlParser = context.getParser("text/html");
						final URI itemLocation = (itemURL == null) ? location : itemURL;
						if (htmlParser != null) try {
							idoc = htmlParser.parse(itemLocation, charset,
									new ByteArrayInputStream(itemContent.getBytes()));
							
						} catch (Exception e) { logger.warn("error parsing feed-item '" + itemLocation + "', ignoring", e); }
					}
					if (idoc == null)
						idoc = new ParserDocument();
					
					if (itemCreator != null && itemCreator.length() > 0)
						authors.append(",").append(itemCreator);
					
					idoc.addHeadline(itemTitle);
					
					if (itemURL != null)
						idoc.addReference(itemURL, itemTitle);
					
					idoc.addText(itemDescr);
					
					pdoc.addSubDocument(itemTitle, idoc);
				}
			}
			
			return pdoc;
		} catch (ParseException e) {
			throw new ParserException("error parsing feed", e);
		}
	}
}
