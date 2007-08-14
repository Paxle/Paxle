package org.paxle.se.index;

import java.io.IOException;
import java.util.Iterator;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;

public interface IIndexIteratable {
	
	/**
	 * Provides an {@link Iterator} iterating over all indexed documents, returning only those
	 * values of the indexed documents associated with the given {@link Field}.
	 * 
	 * @see IIndexerDocument for a list of Paxle's standard fields
	 * @param  <E> the (Java-)Type of the field
	 * @param  field the field to return
	 * @return an {@link Iterator} over all documents limited specifically to the given field
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public <E> Iterator<E> iterator(Field<E> field) throws IOException;
	
	/**
	 * Provides an {@link Iterator} iterating over all indexed documents, returning only those
	 * values of the documents containing the given {@link Stirng} associated with the given {@link Field}.
	 * 
	 * @see IIndexerDocument for a list of Paxle's standard fields
	 * @param  <E> the (Java-)Type of the field
	 * @param  field the field to return
	 * @param  contains a {@link String} all documents to be iterated over have to contain
	 * @return an {@link Iterator} over all documents containing <code>contains</code> limited
	 *         specifically to the given field
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public <E> Iterator<E> iterator(Field<E> field, String contains) throws IOException;
	
	/**
	 * Provides an {@link Iterator} iterating over all indexed documents, directly converting
	 * them into {@link IIndexerDocument}s.
	 * 
	 * @return an {@link Iterator} over all indexed documents in the index
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public Iterator<IIndexerDocument> docIterator() throws IOException;
	
	/**
	 * Provides an {@link Iterator} iterating over all indexed documents containing the given
	 * {@link String}, directly converting them into {@link IIndexerDocument}s.
	 * 
	 * @param contains a {@link String} all documents to be iterated over have to contain
	 * @return an {@link Iterator} over all indexed documents in the index
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public Iterator<IIndexerDocument> docIterator(String contains) throws IOException;
	
	/**
	 * Provides an alphabetic {@link Iterator} iterating over all indexed words.
	 * 
	 * @return an {@link Iterator} over all indexed words, each returned as {@link String}
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public Iterator<String> wordIterator() throws IOException;
	
	/**
	 * Provides an alphabetic {@link Iterator} iterating over all following indexed words starting
	 * with the given string (or if this word does not exist in the index, the directly following one).
	 * 
	 * @param  start the word to start iterating with
	 * @return an {@link Iterator} over all indexed words beginning at <code>start</code>, each
	 *         returned as {@link String}
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public Iterator<String> wordIterator(String start) throws IOException;
	
	/**
	 * Provides an alphabetic {@link Iterator} iterating over all indexed words saved for a given
	 * {@link Field}.
	 * 
	 * @see IIndexerDocument for a list of Paxle's standard fields
	 * @param  field the {@link Field} to limit the iteration to
	 * @return an {@link Iterator} over all words indexed for this {@link Field}
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public Iterator<String> wordIterator(Field<?> field) throws IOException;
	
	/**
	 * Provides an alphabetic {@link Iterator} iterating over the indexed words saved for the a given
	 * {@link Field} starting with the given string (or if this word does not exist in the index, the
	 * directly following one).
	 * 
	 * @see IIndexerDocument for a list of Paxle's standard fields
	 * @param  start the word to start iterating with
	 * @return an {@link Iterator} over all words indexed for this {@link Field} beginning at
	 *         <code>start</code>, each returned as {@link String}
	 * @throws <b>IOException</b> if an IOException occurs
	 */
	public Iterator<String> wordIterator(String start, Field<?> field) throws IOException;
}
