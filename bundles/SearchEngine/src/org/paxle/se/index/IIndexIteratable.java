package org.paxle.se.index;

import java.io.IOException;
import java.util.Iterator;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;

public interface IIndexIteratable {
	
	public <E> Iterator<E> iterator(Field<E> field) throws IOException;
	public <E> Iterator<E> iterator(Field<E> field, String contains) throws IOException;
	public Iterator<IIndexerDocument> docIterator() throws IOException;
	public Iterator<IIndexerDocument> docIterator(String contains) throws IOException;
	public Iterator<String> wordIterator() throws IOException;
	public Iterator<String> wordIterator(String start) throws IOException;
	public Iterator<String> wordIterator(Field<?> field) throws IOException;
	public Iterator<String> wordIterator(String start, Field<?> field) throws IOException;
}
