/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.parser.ASubParser;
import org.paxle.parser.CachedParserDocument;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ParserContext;
import org.paxle.parser.ParserException;
import org.xml.sax.InputSource;

import de.nava.informa.core.ChannelFormat;
import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ImageIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;

/**
 * @scr.component
 * @scr.service interface="org.paxle.parser.ISubParser"
 * @scr.property name="MimeTypes" 
 * 				 values.1="application/rdf+xml"
 * 				 values.2="application/rss+xml"
 * 			     values.3="application/atom+xml"
 * 				 values.4="text/rss"
 */
public class FeedParser extends ASubParser implements ISubParser {
	private static final String MIMETYPE_RDF = "application/rdf+xml";
	private static final String MIMETYPE_RSS = "application/rss+xml";
	private static final String MIMETYPE_ATOM = "application/atom+xml";
	
	/**
	 * for logging
	 */
	private final Log logger = LogFactory.getLog(FeedParser.class);
	
	@Override
	public IParserDocument parse(URI location, String charset, InputStream is) throws ParserException, UnsupportedEncodingException, IOException {				
		
		// the result object
		IParserDocument pdoc = null;		
		try {
			// getting required tools
			final ParserContext context = ParserContext.getCurrentContext();
			final ITempFileManager tempFileManager =  context.getTempFileManager();
			final IReferenceNormalizer refNorm = context.getReferenceNormalizer();

			// creating an empty document
			pdoc = new CachedParserDocument(tempFileManager);
			
			// parse the feed
			final ChannelBuilder builder = new ChannelBuilder();
			final ChannelIF channel = de.nava.informa.parsers.FeedParser.parse(builder, new InputSource(is), location.toURL());
			
			// setting mime-type properly
			ChannelFormat format = channel.getFormat();
			String formatStr = format.toString();
			if (format.equals(ChannelFormat.RSS_1_0)) {
				pdoc.setMimeType(MIMETYPE_RDF);
			} else if (formatStr.toLowerCase().startsWith("rss")) {
				pdoc.setMimeType(MIMETYPE_RSS);
			} else if (formatStr.toLowerCase().startsWith("atom")) {
				pdoc.setMimeType(MIMETYPE_ATOM);
			} 

			// extracting title/summary
			pdoc.setTitle(channel.getTitle());
			pdoc.setSummary(channel.getDescription());
			
			// the channel language
			final String language = channel.getLanguage();
			if (language != null) {
				pdoc.setLanguages(new HashSet<String>(Arrays.asList(language.split(","))));
			}
			
			// the channel image
			final ImageIF image = channel.getImage();
			if (image != null) try {
				pdoc.addImage(image.getLink().toURI(), image.getTitle());
			} catch (URISyntaxException e) { 
				logger.warn("image-link is not valid: '" + image.getLink() + "': " + e.getMessage()); 
			}
			
			// channel last-updated date
			if (channel.getLastUpdated() != null) {
				pdoc.setLastChanged(channel.getLastUpdated());
			} else if (channel.getLastBuildDate() != null) {
				pdoc.setLastChanged(channel.getLastBuildDate());
			}
			
			final Collection<ItemIF> items = channel.getItems();
			if (!items.isEmpty()) {
				final StringBuilder authors = new StringBuilder();
				
				// loop through all items
				final Iterator<ItemIF> it = items.iterator();				
				while (it.hasNext()) {
					IParserDocument idoc = null;
					final ItemIF item = it.next();
					
					// the item location
					final URI itemURL = (item.getLink()==null)
									  ? null
									  : refNorm.normalizeReference(item.getLink().toExternalForm());					
					
					// the item content (if available)
					String itemContent = item.getElementValue("content");
					if (itemContent == null || itemContent.length() == 0) {
						itemContent = item.getElementValue("content:encoded");
					}
					if ((itemContent != null) && (itemContent.length() > 0)) {						
						final URI itemLocation = (itemURL == null) ? location : itemURL;
						
						// parsing the html content
						final ISubParser htmlParser = context.getParser("text/html");
						if (htmlParser != null) {
							try {
								// parsing item content
								idoc = htmlParser.parse(
										itemLocation, 
										charset,
										new ByteArrayInputStream(itemContent.getBytes("UTF-8"))
								);		
								
								if (idoc != null && idoc.getMimeType() == null) {
									idoc.setMimeType("text/html");
								}
							} catch (Exception e) { 
								logger.warn("error parsing feed-item '" + itemLocation + "', ignoring", e); 
							}
						}
					}
					if (idoc == null) {
						idoc = new ParserDocument();
						idoc.setMimeType("text/plain");
					}
					
					// the item author
					final String itemCreator = item.getCreator();
					if (itemCreator != null && itemCreator.length() > 0) {
						authors.append(",").append(itemCreator);
						idoc.setAuthor(itemCreator);
					}
					
					// the item headline
					String itemTitle = item.getTitle();
					if (itemTitle != null) {
						idoc.addHeadline(itemTitle);
					}
					
					
					// the item URL
					if (itemURL != null) {
						idoc.addReference(itemURL, itemTitle, "ParserFeed");
					}
					
					// the item description
					final String itemDescr = item.getDescription();
					if (itemDescr != null) {
						idoc.addText(itemDescr);
					}
					
					// the item date
					if (item.getDate() != null) {
						idoc.setLastChanged(item.getDate());
					}
					
					pdoc.addSubDocument(itemTitle, idoc);
				}
								
				// adding the author list 
				if (authors.length() > 0) {
					if (authors.charAt(0) == ',') authors.deleteCharAt(0);
					pdoc.setAuthor(authors.toString());
				}
			}
			
			pdoc.setStatus(IParserDocument.Status.OK);
			return pdoc;
		} catch (Throwable e) {
			throw new ParserException("error parsing feed", e);
		}
	}
}
