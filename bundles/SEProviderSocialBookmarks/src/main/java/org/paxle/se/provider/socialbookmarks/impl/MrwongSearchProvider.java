package org.paxle.se.provider.socialbookmarks.impl;


import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;

import de.nava.informa.core.ChannelIF;
import de.nava.informa.impl.basic.ChannelBuilder;
import de.nava.informa.impl.basic.Item;
import de.nava.informa.parsers.FeedParser;

public class MrwongSearchProvider implements ISearchProvider {

	public MrwongSearchProvider(){
	}
	public ITokenFactory getTokenFactory() {
		return new PaxleInfrastructureTokenFactor();
	}
	
	private String joinWords(String[] words){
		String phrase="";
		for(int i=1;i<words.length-1;i++)
			phrase+=words[i]+" ";
		phrase+=words[words.length-1];
		return phrase;
	}
	
	public void search(String request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {
		try {
			//creating a channel-builder
	        ChannelBuilder builder = new ChannelBuilder();   
	        
	        // parsing the rss/atom feed
	        ChannelIF channel = FeedParser.parse(builder, new URL("http://mr-wong.com/rss/tag/"+request));
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
				indexerDoc.set(IIndexerDocument.AUTHOR, item.getCreator());
				indexerDoc.set(IIndexerDocument.LAST_MODIFIED, item.getDate());
				results.add(indexerDoc);
	        }

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
