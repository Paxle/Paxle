package org.paxle.se.index.lucene.impl;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.IndexException;
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
	
	public IIndexerDocument[] search(IToken searchToken) throws IOException,
			IndexException {
		/* TODO:
		 * - export the IToken-tree to lucene-query syntax
		 * - search the lucene db
		 * - collect all Hits into IIndexerDocuments using the Converter */
		return null;
	}
}
