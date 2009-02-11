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
package org.paxle.se.provider.rsssearch.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.core.ItemIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.parsers.FeedParser;

public class RssSearchProvider implements ISearchProvider,ManagedService {
	
	// the paxle default
	private static final String DEFAULT_CHARSET = "UTF-8";

	/**
	 * The base URL to connect to
	 */
	String feedURL;

	/**
	 * for logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	public RssSearchProvider(String feedURL){
		this.feedURL=feedURL;
		
		this.logger.debug(String.format(
				"Registering an RSS-Search-Provider using the base-URL: %s",
				this.feedURL
		));
	}
	
	public void search(AToken token, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {
        String url = null;
		try {
			String request=new RssSearchQueryFactor().transformToken(token);
			//creating a channel-builder
	        ChannelBuilder builder = new ChannelBuilder();   
	        
	        // parsing the rss/atom feed
	        HttpMethod hm = null;
			try {
				// opening an http connection
				url = new URL(String.format(feedURL, URLEncoder.encode(request, DEFAULT_CHARSET))).toExternalForm();
				hm = new GetMethod(url);
				HttpClient hc = new HttpClient();
				int status = hc.executeMethod(hm);
				if (status != 200) {
					this.logger.warn(String.format(
							"Error while connecting to '%s'.\r\n\tServer-Status: %s",
							url,
							hm.getStatusLine()
					));
					return;
				}

				// parsing the rss/atom feed
				ChannelIF channel = FeedParser.parse(builder, hm.getResponseBodyAsStream());
				
				// loop through all items
				Collection<ItemIF> items = channel.getItems();
		        Iterator<ItemIF> it=items.iterator();
		        
		        int count=0;
		        while(it.hasNext() && count++<maxCount){
		        	ItemIF item=it.next();
		        	
		        	IIndexerDocument indexerDoc = new IndexerDocument();
					indexerDoc.set(IIndexerDocument.LOCATION, item.getLink().toString());
					indexerDoc.set(IIndexerDocument.TITLE, item.getTitle());
					indexerDoc.set(IIndexerDocument.PROTOCOL, item.getLink().getProtocol());
					indexerDoc.set(IIndexerDocument.SUMMARY, item.getDescription());
					indexerDoc.set(IIndexerDocument.AUTHOR, item.getCreator()==null?"":item.getCreator());
					indexerDoc.set(IIndexerDocument.LAST_MODIFIED, item.getDate());
					
					results.add(indexerDoc);
		        }				
	        } catch (IOException e){
	        	//do nothing, it just not worked (offline or rss-site problem)
				this.logger.warn(e);
	        } finally {
	        	if (hm != null) hm.releaseConnection();
	        }

		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected '%s' while connecting to '%s'.",
					e.getClass().getName(),
					(url==null)?this.feedURL:url
			));
		}
	}

	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}
}
