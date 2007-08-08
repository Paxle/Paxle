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
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneSearcher;
import org.paxle.se.query.ITokenFactory;

public class LuceneSearcher implements ILuceneSearcher, Closeable {
	
	private final IndexSearcher searcher;
	private final LuceneTokenFactory ltf;
	
	public LuceneSearcher(String path) throws IOException {
		this.searcher = new IndexSearcher(path);
		this.ltf = new LuceneTokenFactory();
	}
	
	public void search(String request, List<IIndexerDocument> results, int maxCount) throws IOException {
		final QueryParser queryParser = new QueryParser("*", new StandardAnalyzer());
		final Query query;
		try {
			query = queryParser.parse(request);
		} catch (org.apache.lucene.queryParser.ParseException e) {
			final Token token = e.currentToken;
			throw new IndexException(
					"error parsing query string '" + request + "' at '" + token.image + "', lines "
					+ token.beginLine + "-" + token.endLine + ", columns "
					+ token.beginColumn + "-" + token.endColumn, e);
		}
		this.searcher.search(query, new IIndexerDocHitCollector(results, maxCount));
	}
	
	private class IIndexerDocHitCollector extends HitCollector {
		
		private final List<IIndexerDocument> results;
		private final int max;
		private int current = 0;
		
		public IIndexerDocHitCollector(List<IIndexerDocument> results, int max) {
			this.results = results;
			this.max = max;
		}
		
		@Override
		public void collect(int doc, float score) {
			if (this.current++ < this.max) try {
				this.results.add(Converter.luceneDoc2IIndexerDoc(LuceneSearcher.this.searcher.doc(doc)));
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public ITokenFactory getTokenFactory() {
		return this.ltf;
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
	
	public Iterator<String> wordIterator(Field<?> field) throws IOException {
		return new FieldLimitedWordIterator(this.searcher.getIndexReader().terms(new Term(field.getName(), "")), field);
	}
	
	public Iterator<String> wordIterator(String start, Field<?> field) throws IOException {
		return new FieldLimitedWordIterator(this.searcher.getIndexReader().terms(new Term(field.getName(), start)), field);
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
		
		protected final TermEnum tenum;
		protected Term current;
		protected Term next;
		
		public WordIterator(TermEnum tenum) {
			this.tenum = tenum;
			this.next = this.tenum.term();
		}
		
		protected Term next0() {
			try {
				final Term lnext = this.next;
				this.next = (this.tenum.next()) ? this.tenum.term() : null;
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
				return this.current.text();
			}
		}
		
		public void remove() {
			// XXX: is urgently needed, but I don't know how to do currently
			throw new UnsupportedOperationException();
		}
	}
	
	private static class FieldLimitedWordIterator extends WordIterator implements Iterator<String> {
		
		protected final Field<?> field;
		
		public FieldLimitedWordIterator(TermEnum tenum, Field<?> field) {
			super(tenum);
			this.field = field;
		}
		
		@Override
		protected Term next0() {
			Term ret;
			do {
				ret = super.next0();
			} while (super.next != null && !super.next.field().equals(this.field.getName()));
			return ret;
		}
	}
}
