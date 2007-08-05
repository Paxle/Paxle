package org.paxle.se.index.lucene.impl;

import java.io.IOException;
import java.text.ParseException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.Token;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneSearcher;
import org.paxle.se.query.IToken;

public class LuceneSearcher extends IndexSearcher implements ILuceneSearcher {
	
	/** @see IndexSearcher#IndexSearcher(String) */
	public LuceneSearcher(String path) throws CorruptIndexException,
			IOException {
		super(path);
	}
	
	/** @see IndexSearcher#IndexSearcher(Directory) */
	public LuceneSearcher(Directory directory) throws CorruptIndexException,
			IOException {
		super(directory);
	}
	
	/** @see IndexSearcher#IndexSearcher(IndexReader) */
	public LuceneSearcher(IndexReader r) {
		super(r);
	}
	
	public IIndexerDocument[] search(IToken searchToken, int maxCount, Field<?> defaultField) throws IOException,
			IndexException, ParseException {
		final String queryString = searchToken.getString();
		final QueryParser queryParser = new QueryParser(defaultField.getName(), new StandardAnalyzer());
		final Query query;
		try {
			query = queryParser.parse(queryString);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			final Token token = e.currentToken;
			throw new ParseException(
					"error parsing query string '" + queryString + "' at lines "
					+ token.beginLine + "-" + token.endLine + ", columns "
					+ token.beginColumn + "-" + token.endColumn,
					token.beginColumn);
		}
		
		final Hits hits = super.search(query/*, TODO */);
		int count = Math.min(hits.length(), maxCount);
		final IIndexerDocument[] docs = new IIndexerDocument[count];
		for (int i=0; i<count; i++)
			docs[i] = Converter.luceneDoc2IIndexerDoc(hits.doc(i));
		return docs;
	}
	
	public int getDocCount() {
		return super.getIndexReader().numDocs();
	}
}
