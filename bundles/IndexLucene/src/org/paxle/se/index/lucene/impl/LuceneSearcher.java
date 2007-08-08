package org.paxle.se.index.lucene.impl;

import java.io.Closeable;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.queryParser.Token;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneSearcher;
import org.paxle.se.query.ITokenFactory;
import org.paxle.se.query.tokens.AToken;

public class LuceneSearcher implements ILuceneSearcher, Closeable {
	
	private final IndexSearcher searcher;
	
	public LuceneSearcher(String path) throws IOException {
		this.searcher = new IndexSearcher(path);
	}
	
	public void search(String request, List<IIndexerDocument> results, int maxCount) {
		try {
			final Hits hits = search(request);
			final int count = Math.min(hits.length(), maxCount);
			for (int i=0; i<count; i++)
				results.add(Converter.luceneDoc2IIndexerDoc(hits.doc(i)));
		} catch (IOException e) {
		} catch (IndexException e) {
		} catch (ParseException e) {
		}
	}
	
	public ITokenFactory getTokenFactory() {
		return new LuceneTokenFactory();
	}
	
	private Hits search(String queryString) throws IOException, ParseException, IndexException {
		final QueryParser queryParser = new QueryParser("*", new StandardAnalyzer());
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
		
		return this.searcher.search(query/*, TODO */);
	}
	
	public IIndexerDocument[] search(AToken searchToken, int maxCount) throws IOException, IndexException, ParseException {
		final Hits hits = search(searchToken.getString());
		int count = Math.min(hits.length(), maxCount);
		final IIndexerDocument[] docs = new IIndexerDocument[count];
		for (int i=0; i<count; i++)
			docs[i] = Converter.luceneDoc2IIndexerDoc(hits.doc(i));
		return docs;
	}
	
	public int getDocCount() {
		return this.searcher.getIndexReader().numDocs();
	}
	
	public <E> Iterator<E> iterator(Field<E> field) throws IOException {
		return new FieldIterator<E>(new DocumentIterator(this.searcher.getIndexReader()), field);
	}
	
	public <E> Iterator<E> iterator(Field<E> field, String contains) throws IOException {
		return new FieldIterator<E>(new DocumentIterator(this.searcher.getIndexReader(), contains), field);
	}
	
	public Iterator<IIndexerDocument> docIterator() throws IOException {
		return new DocumentIterator(this.searcher.getIndexReader());
	}
	
	public Iterator<IIndexerDocument> docIterator(String contains) throws IOException {
		return new DocumentIterator(this.searcher.getIndexReader(), contains);
	}
	
	public Iterator<String> wordIterator() throws IOException {
		return new WordIterator(this.searcher.getIndexReader().terms());
	}
	
	public Iterator<String> wordIterator(String start) throws IOException {
		return new WordIterator(this.searcher.getIndexReader().terms(new Term("*", start)));
	}
	
	public void close() throws IOException {
		this.searcher.close();
	}
	
	private static class DocumentIterator implements Iterator<IIndexerDocument> {
		
		private final IndexReader reader;
		private final TermDocs tenum;
		private int current = -1;
		private int next;
		
		public DocumentIterator(IndexReader reader) throws IOException {
			this(reader, null);
		}
		
		public DocumentIterator(IndexReader reader, String word) throws IOException {
			this.reader = reader;
			this.tenum = (word == null) ? reader.termDocs() : reader.termDocs(new Term("*", word));
			this.next = this.tenum.doc();
		}
		
		private int next0() {
			try {
				final int lnext = this.next;
				this.next = (this.tenum.next()) ? this.tenum.doc() : -1;
				return lnext;
			} catch (IOException e) {
				throw new RuntimeException("I/O error iterating through terms", e);
			}
		}
		
		public boolean hasNext() {
			return this.next >= 0;
		}
		
		public IIndexerDocument next() {
			this.current = this.next;
			if (this.current < 0) {
				throw new NoSuchElementException();
			} else try {
				this.next = next0();
				return Converter.luceneDoc2IIndexerDoc(this.reader.document(this.current));
			} catch (IOException e) {
				throw new RuntimeException("I/O error iterating through documents", e);
			} catch (ParseException e) {
				throw new RuntimeException("Converter error iterating through documents", e);
			}
		}
		
		public void remove() {
			try {
				this.reader.deleteDocument(this.current);
			} catch (IOException e) {
				throw new RuntimeException("I/O error removing document", e);
			}
		}
	}
	
	private static class FieldIterator<E> implements Iterator<E> {
		
		private final Field<E> field;
		private final DocumentIterator it;
		
		public FieldIterator(DocumentIterator it, Field<E> field) {
			this.field = field;
			this.it = it;
		}
		
		public boolean hasNext() {
			return this.it.hasNext();
		}
		
		public E next() {
			return this.it.next().get(this.field);
		}
		
		public void remove() {
			// It is doable, but I'd have to rebuild the whole index-lucene bundle for it
			// to get an reference to a currently active writer
			throw new UnsupportedOperationException();
		}
	}
	
	private static class WordIterator implements Iterator<String> {
		
		private final TermEnum tenum;
		private String current;
		private String next;
		
		public WordIterator(TermEnum tenum) {
			this.tenum = tenum;
			this.next = this.tenum.term().text();
		}
		
		private String next0() {
			try {
				final String lnext = this.next;
				this.next = (this.tenum.next()) ? this.tenum.term().text() : null;
				return lnext;
			} catch (IOException e) {
				throw new RuntimeException("I/O error iterating through terms", e);
			}
		}
		
		public boolean hasNext() {
			return this.next != null;
		}
		
		public String next() {
			this.current = this.next;
			if (this.current == null) {
				throw new NoSuchElementException();
			} else {
				this.next = next0();
				return this.current;
			}
		}
		
		public void remove() {
			// XXX: is urgently needed, but I don't know how to do currently
			throw new UnsupportedOperationException();
		}
	}
}
