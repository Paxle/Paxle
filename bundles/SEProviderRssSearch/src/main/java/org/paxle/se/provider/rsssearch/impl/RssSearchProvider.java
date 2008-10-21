/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.se.query.tokens.AToken;
import org.paxle.se.search.ISearchProvider;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.parsers.FeedParser;

public class RssSearchProvider implements ISearchProvider,ManagedService {
	
	// the paxle default
	private static final String DEFAULT_CHARSET = "UTF-8";
	
	String feedURL;
	public static List<ServiceRegistration> providers;

	
	public RssSearchProvider(String feedURL){
		this.feedURL=feedURL;
		System.out.println(feedURL);
	}
	
	@SuppressWarnings("unchecked")
	public void search(AToken token, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {
		try {
			String request=RssSearchQueryFactor.transformToken(token, new RssSearchQueryFactor());
			//creating a channel-builder
	        ChannelBuilder builder = new ChannelBuilder();   
	        
	        // parsing the rss/atom feed
			try {
				// opening an http connection
				HttpMethod hm = new GetMethod(new URL(String.format(feedURL, URLEncoder.encode(request, DEFAULT_CHARSET))).toExternalForm());
				HttpClient hc = new HttpClient();
				int status = hc.executeMethod(hm);
				if (status != 200) {
					System.out.println("no status 200 - maybe something went wrong");
					return;
				}

				// parsing the rss/atom feed
				ChannelIF channel = FeedParser.parse(builder, hm.getResponseBodyAsStream());
				Collection<Item> items = channel.getItems();
	        Iterator<Item> it=items.iterator();
	        int count=0;
	        IIndexerDocument indexerDoc;
	        while(it.hasNext() && count++<maxCount){
	        	Item item=it.next();
				indexerDoc = new IndexerDocument();
				indexerDoc.set(IIndexerDocument.LOCATION, item.getLink().toString());
				indexerDoc.set(IIndexerDocument.TITLE, item.getTitle());
				indexerDoc.set(IIndexerDocument.PROTOCOL, item.getLink().getProtocol());
				indexerDoc.set(IIndexerDocument.SUMMARY, item.getDescription());
				indexerDoc.set(IIndexerDocument.AUTHOR, item.getCreator()==null?"":item.getCreator());
				indexerDoc.set(IIndexerDocument.LAST_MODIFIED, item.getDate());
				results.add(indexerDoc);
	        }
			hm.releaseConnection();
	        }catch (IOException e){
	        	//do nothing, it just not worked (offline or rss-site problem)
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * read the list of RSS-URLs
	 * @return an ArrayList with URLs
	 * @throws IOException
	 */
	public static ArrayList<String> getUrls() throws IOException{
		File rssList=new File("rssProviders.txt");
		if(!rssList.exists()){
			ArrayList<String> defaults=new ArrayList<String>();
			defaults.add("http://del.icio.us/rss/tag/%s");
			defaults.add("http://www.mister-wong.com/rss/tags/%s");
			setUrls(defaults);
		}
		BufferedReader is=new BufferedReader(new InputStreamReader(new FileInputStream(rssList)));
		String line;
		ArrayList<String> list=new ArrayList<String>();
		while((line=is.readLine())!=null){
			list.add(line);
		}
		is.close();
		return list;
	}
	public static void setUrls(ArrayList<String> urls) throws IOException{
		File rssList=new File("rssProviders.txt");
		if(!rssList.exists()){
			rssList.createNewFile();
		}
		BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(rssList));
		Iterator<String> it = urls.iterator();
		while(it.hasNext()){
			os.write((it.next()+"\n").getBytes());
		}
		os.close();
	}

	public static void registerSearchers(ArrayList<String> urls){
		Iterator<ServiceRegistration> prov_it=providers.iterator();
		while(prov_it.hasNext()){
			ServiceRegistration sr=prov_it.next();
			try{
				sr.unregister();
			}catch(IllegalStateException e){} //service is already unregistered
		}
		Iterator<String> it=urls.iterator();
		while(it.hasNext()){
			providers.add(Activator.bc.registerService(ISearchProvider.class.getName(),
				new RssSearchProvider(it.next()), new Hashtable<String,String>())
			);
		}
	}
}
