package org.paxle.se.provider.google.impl;

import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;

import com.google.soap.search.GoogleSearch;
import com.google.soap.search.GoogleSearchResult;
import com.google.soap.search.GoogleSearchResultElement;

public class GoogleSoapSearchProvider implements ISearchProvider {

	private String googleKey = null;
	private String soapServiceURL = null;
	
	public ITokenFactory getTokenFactory() {
		return new GoogleSoapTokenFactor();
	}
	
	public GoogleSoapSearchProvider(String googleKey) {
		this(googleKey,null);
	}
	
	public GoogleSoapSearchProvider(String googleKey, String serviceUrl) {
		if (googleKey == null) throw new NullPointerException("Google key was null");
		this.googleKey = googleKey;
		this.soapServiceURL = serviceUrl;
	}

	public void search(String request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException, InterruptedException {
		try {
			// create a new google search object
			GoogleSearch newSearch = new GoogleSearch();
			
			// add a dummy key
	        newSearch.setKey(this.googleKey);
	        
	        // configure endpoint (if specified)
	        if (soapServiceURL != null) {
	        	newSearch.setSoapServiceURL(soapServiceURL);
	        }
	        
	        // set amount of search results that should be returned
	        newSearch.setMaxResults(maxCount);
	        
	        // set query string
	        newSearch.setQueryString(request);
	        
	        // configure the proxy if required
	        // this.configureProxy(newSearch);
	        
	        // do search
	        GoogleSearchResult newSearchResult = newSearch.doSearch();
	        
	        // fetch result
	        GoogleSearchResultElement[] items = newSearchResult.getResultElements();
	        for (GoogleSearchResultElement item: items) {
	        	IIndexerDocument indexerDoc = new IndexerDocument();
	        	
	        	// item URL
                String searchResultURL = item.getURL();
                if (searchResultURL == null || searchResultURL.length() == 0) continue; 
                else {
                	indexerDoc.set(IIndexerDocument.LOCATION,searchResultURL);
                }
                
                // item title
                String searchResultTitle = item.getTitle();
                if (searchResultURL != null || searchResultURL.length() > 0) {
                	indexerDoc.set(IIndexerDocument.TITLE, searchResultTitle);
                }
                	
                String searchResultSize = item.getCachedSize();
                if (searchResultSize != null || searchResultSize.length() > 0) {                	
                	try {
                		Long size = 0l;
                		long mul = 1;
                		if (searchResultSize.endsWith("k")) {
                			searchResultSize = searchResultSize.substring(0,searchResultSize.length()-1);
                			mul = 1024;                			
                		} else if (searchResultSize.endsWith("m")) {
                			searchResultSize = searchResultSize.substring(0,searchResultSize.length()-1);
                			mul = 1024 * 1024;
                		}
                		size = Long.valueOf(searchResultSize) * mul;      
                		indexerDoc.set(IIndexerDocument.SIZE,size);
                	} catch (NumberFormatException e) {}
                }
                
                String searchResultSummary = item.getSummary();
                if (searchResultSummary != null || searchResultSummary.length() > 0) {
                	indexerDoc.set(IIndexerDocument.SUMMARY,searchResultSummary);
                }                
                
                //String searchResultHost = item.getHostName();
                String searchResultSnippet = item.getSnippet();
                if (searchResultSnippet != null || searchResultSnippet.length() > 0) {
                	indexerDoc.set(IIndexerDocument.SNIPPET,searchResultSnippet);
                } 
	        }
	        
	        // print result
	        System.out.println(newSearchResult.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
