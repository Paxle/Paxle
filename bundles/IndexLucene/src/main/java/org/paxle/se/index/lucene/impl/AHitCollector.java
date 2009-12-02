/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.se.index.lucene.impl;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;

/**
 * TODO: do we really need this class. Maybe we should use {@link TopDocs} or a {@link TopDocsCollector} instead.
 * 
 * This function was migrated from an {@link org.apache.lucene.search.HitCollector} and uses additional methods
 * as seen in the {@link org.apache.lucene.search.HitCollectorWrapper}
 */
public abstract class AHitCollector extends Collector {
	protected int base = 0;
	protected Scorer scorer = null;
	protected IndexSearcher searcher;
	
	public final void init(IndexSearcher searcher) {
		this.searcher = searcher;
	}
	
	public final void reset() {
		this.searcher = null;
	}
	
	public void setNextReader(IndexReader reader, int docBase) {
		base = docBase;
	}
	
	public void collect(int doc) throws IOException {
		this.collect(doc + base, scorer.score());
	}
	
	public abstract void collect(int doc, float score);

	public void setScorer(Scorer scorer) throws IOException {
		this.scorer = scorer;      
	}

	public boolean acceptsDocsOutOfOrder() {
		return false;
	}
}
