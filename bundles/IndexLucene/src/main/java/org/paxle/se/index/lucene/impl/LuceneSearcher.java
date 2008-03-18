package org.paxle.se.index.lucene.impl;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneSearcher;
import org.paxle.se.query.IQueryFactory;
import org.paxle.se.query.tokens.AToken;

public class LuceneSearcher implements ILuceneSearcher, Closeable {
	
	private final Log logger = LogFactory.getLog(LuceneSearcher.class);
	private final LuceneQueryFactory ltf = new LuceneQueryFactory();
	private final AFlushableLuceneManager manager;
	
	public LuceneSearcher(AFlushableLuceneManager manager) {
		this.manager = manager;
	}
	
	/**
	 * TODO: how to handle the timeout properly?
	 */
	public void search(AToken request, List<IIndexerDocument> results, int maxCount, long timeout) throws IOException {
		final QueryParser queryParser = new QueryParser(IIndexerDocument.TEXT.getName(), new StandardAnalyzer());
		final Query query;
		try {
			query = queryParser.parse(IQueryFactory.transformToken(request, ltf));
		} catch (org.apache.lucene.queryParser.ParseException e) {
			throw new IndexException("error parsing query string '" + request + "'", e);
		}
		this.logger.debug("searching for query '" + query + "' (" + request + ")");
		this.manager.search(query, new IIndexerDocHitCollector(results, maxCount));
	}
	
	private class IIndexerDocHitCollector extends AHitCollector {
		
		private final List<IIndexerDocument> results;
		private final int max;
		private int current = 0;
		
		public IIndexerDocHitCollector(List<IIndexerDocument> results, int max) {
			this.results = results;
			this.max = max;
		}
		
		@Override
		public void collect(int doc, float score) {
			LuceneSearcher.this.logger.debug("collecting search result " + this.current + "/" + this.max + ", document id '" + doc + "', score: " + score);
			if (this.current++ < this.max) try {
				final Document rdoc = this.searcher.doc(doc);
				this.results.add(Converter.luceneDoc2IIndexerDoc(rdoc));
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close() throws IOException {
		this.manager.close();
	}
	
	public int getDocCount() throws IOException {
		return this.manager.getDocCount();
	}
}
