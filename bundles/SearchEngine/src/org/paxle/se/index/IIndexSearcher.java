package org.paxle.se.index;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.search.ISearchProvider;

public interface IIndexSearcher extends ISearchProvider, Closeable {
	
	public void search(String request, List<IIndexerDocument> results, int maxCount) throws IOException, InterruptedException;
	public ITokenFactory getTokenFactory();
	
	public int getDocCount();
	
	public <E> Iterator<E> iterator(Field<E> field) throws IOException;
	public <E> Iterator<E> iterator(Field<E> field, String contains) throws IOException;
	public Iterator<IIndexerDocument> docIterator() throws IOException;
	public Iterator<IIndexerDocument> docIterator(String contains) throws IOException;
	public Iterator<String> wordIterator() throws IOException;
	public Iterator<String> wordIterator(String start) throws IOException;
	public Iterator<String> wordIterator(Field<?> field) throws IOException;
	public Iterator<String> wordIterator(String start, Field<?> field) throws IOException;
	
	public void close() throws IOException;
}
