package org.paxle.se.index.lucene;

import org.paxle.se.index.IIndexIteratable;
import org.paxle.se.index.IIndexSearcher;
import org.paxle.se.search.ISearchProvider;

public interface ILuceneSearcher extends IIndexSearcher, IIndexIteratable, ISearchProvider {

}
