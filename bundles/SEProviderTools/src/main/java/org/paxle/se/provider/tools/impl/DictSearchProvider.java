package org.paxle.se.provider.tools.impl;

import java.io.IOException;
import java.util.List;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;

public class DictSearchProvider implements ISearchProvider {

	public DictSearchProvider(){
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
			IIndexerDocument indexerDoc;
			String[] words = request.split(" ");
			if(words[0].equals("en-de")){
				indexerDoc = new IndexerDocument();
				String phrase=joinWords(words);
				indexerDoc.set(IIndexerDocument.LOCATION, "http://dict.leo.org/?search="+phrase+"&searchLoc=-1&lp=ende&lang=de");
				indexerDoc.set(IIndexerDocument.TITLE, "Lookup english phrase \""+phrase+"\" at dict.leo.org");
				results.add(indexerDoc);
			}else if(words[0].equals("de-en")){
				indexerDoc = new IndexerDocument();
				String phrase=joinWords(words);
				indexerDoc.set(IIndexerDocument.LOCATION, "http://dict.leo.org/?search="+phrase+"&searchLoc=1&lp=ende&lang=de");
				indexerDoc.set(IIndexerDocument.TITLE, "Lookup german phrase \""+phrase+"\" at dict.leo.org");
				results.add(indexerDoc);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
