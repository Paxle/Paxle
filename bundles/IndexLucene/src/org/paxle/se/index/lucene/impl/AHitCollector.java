package org.paxle.se.index.lucene.impl;

import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;

public abstract class AHitCollector extends HitCollector {
	
	protected IndexSearcher searcher;
	
	public final void init(IndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	public final void reset() {
		this.searcher = null;
	}
}
