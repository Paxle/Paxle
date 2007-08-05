package org.paxle.se;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IndexException;

public interface ISearchEngine {
	
	public abstract IIndexerDocument[] doSearch(String paxleQuery, int count) throws DBUnitializedException, IndexException;
	
	public abstract int getIndexedDocCount() throws DBUnitializedException, IndexException;
}
