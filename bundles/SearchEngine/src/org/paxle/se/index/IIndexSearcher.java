package org.paxle.se.index;

import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.tokens.AToken;

public interface IIndexSearcher {
	
	public static final String QUERY_TOKEN_FACTORY = "query.token.factory";
	
	public IIndexerDocument[] search(AToken searchToken, int maxCount) throws IOException, IndexException, ParseException;
	
	public int getDocCount();
	
	public <E> Iterator<E> iterator(Field<E> field) throws IOException;
	public <E> Iterator<E> iterator(Field<E> field, String contains) throws IOException;
	public Iterator<IIndexerDocument> docIterator() throws IOException;
	public Iterator<IIndexerDocument> docIterator(String contains) throws IOException;
	public Iterator<String> wordIterator() throws IOException;
	public Iterator<String> wordIterator(String start) throws IOException;
}
