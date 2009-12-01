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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.IIndexIteratable;

@Component(immediate=true, metatype=false)
@Services({
	@Service(IIndexIteratable.class),
	@Service(ILuceneManager.class)
})
@Property(name="dataPath",value="lucene-db")
public class AFlushableLuceneManager implements IIndexIteratable, ILuceneManager {
	
	/**
	 * A {@link Term}-object with the {@link IIndexerDocument#LOCATION}-field and a <code>null</code>-value.
	 * It is used to minimize the overhead when creating a new Term-object to update documents.
	 * @see #write(Document, Analyzer) 
	 */
	static final Term CACHED_LOCATION_TERM = new Term(IIndexerDocument.LOCATION.getName(), null);
	
	/**
	 * For logging
	 */
	protected final Log logger = LogFactory.getLog(AFlushableLuceneManager.class);	
	
	public final ReentrantReadWriteLock rwlock = new ReentrantReadWriteLock(true);
	public final ReentrantReadWriteLock.ReadLock rlock = this.rwlock.readLock();
	public final ReentrantReadWriteLock.WriteLock wlock = this.rwlock.writeLock();
	
	/**
	 * A factory to create new {@link IIndexerDocument indexer-documents}
	 */
	@Reference(target="(docType=org.paxle.core.doc.IIndexerDocument)")
	protected IDocumentFactory docFactory;
	
	@Reference
	protected IFieldManager fieldManager;	
	
	@Reference
	protected IStopwordsManager stopWordsManager;
	
	/**
	 * The default {@link Analyzer}
	 */
	protected PaxleAnalyzer analyzer;
	
	protected String fullPath;
	
	protected File dataPath;
	
	protected IndexWriter writer;

	protected IndexReader reader;
	
	private boolean dirty = false;
	
	/**
	 * the amount of known documents
	 */
	private int docCount = -1;
	
	/**
	 * A timer to periodically trigger a writer flush
	 */
	private Timer flushTimer;

	protected void activate(Map<String, Object> props) throws IOException, InterruptedException {
		// the default analyzer to use
		this.analyzer = this.stopWordsManager.getDefaultAnalyzer();
		
		// init the converter
		// TODO: this is bad. we need to remove this
		Converter.fieldManager = this.fieldManager;
		
		// the path were the data should be stored
		//TO-DO: check props for null
		this.fullPath = System.getProperty("paxle.data") + File.separatorChar + props.get("dataPath");
		this.dataPath = new File(this.fullPath);
		
		final File writeLock = new File(this.fullPath, "write.lock");
		if (writeLock.exists()) {
			logger.warn(
				"Lucene index directory is locked, removing lock. " +
				"Shutdown now if any other lucene-compatible application currently accesses the directory '" +
				writeLock.getPath()
			);
			Thread.sleep(5000l);
			writeLock.delete();
		}
		writeLock.deleteOnExit();
		
		// opening index-reader and -writer
		final Directory dir = FSDirectory.open(this.dataPath);
		this.writer = new IndexWriter(dir, analyzer, MaxFieldLength.UNLIMITED);
		this.reader = IndexReader.open(dir, true);		// open a read-only index, deletions are performed by the writer
				
		// getting the amount of known documents
		this.docCount = reader.numDocs();
		
		// starting a flush timer
		this.flushTimer = new Timer("LuceneFlushTimer");
		this.flushTimer.scheduleAtFixedRate(new FlushTimerTask(), 60*60*1000, 60*60*1000);
		
		// set merge policy
		LogByteSizeMergePolicy policy = new LogByteSizeMergePolicy(this.writer);
		policy.setMaxMergeMB(2048);
		this.writer.setMergePolicy(policy);
	}
	
	protected void deactivate() throws IOException {
		// canceling the flush timer
		this.flushTimer.cancel();		
		// XXX should we wait for a while here?
		
		// closing readers and writers
		this.wlock.lock();
		try {
			this.writer.close();
			this.reader.close();
		} finally { 
			this.wlock.unlock(); 
		}
	}
	
	/**
	 * This function 
	 * <ul>
	 * 	<li>closes an old {@link IndexReader index-reader}</li>
	 * 	<li>causes the {@link IndexWriter index-writer} to flush recently added data</li>
	 * 	<li>creates a new {@link IndexReader index-reader}</li>
	 * </ul>
	 * 
	 * Re-opening of the index-reader is required because the index-reader 
	 * <i>"only searches the index as of the "point in time" that it was opened.
	 * Any updates to the index, either added or deleted documents, will not 
	 * be visible until the IndexReader is re-opened."</i><br />
	 * See: 
	 * <a href="http://wiki.apache.org/lucene-java/LuceneFAQ#head-6c56b0449d114826586940dcc6fe51582676a36e">
	 * 	Lucene FAQ - Does Lucene allow searching and indexing simultaneously?
	 * </a>
	 * 
	 * @throws IOException
	 */
	void flush() throws IOException {
		this.logger.debug("Flushing index writer and reopening index reader");
		this.writer.commit();
		IndexReader newReader = this.reader.reopen();
		if (newReader != reader) {
			reader.close(); 
		}
		this.reader = newReader;
	}
	
	protected void checkFlush() throws IOException {
		this.rlock.lock();
		try {
			if (this.dirty) {
				this.rlock.unlock();
				this.wlock.lock();
				try {
					if (this.dirty) {
						flush();
						this.dirty = false;
					}
				} finally {
					this.rlock.lock();
					this.wlock.unlock();
				}
			}
		} finally {
			this.rlock.unlock();
		}
	}
	
	protected boolean isDirty() {
		return this.dirty;
	}
	
	protected void setDirty() {
		this.dirty = true;
	}

	public Document getDocument(int doc) throws IOException {
		this.rlock.lock();
		try {
			return this.reader.document(doc);
		} finally { this.rlock.unlock(); } 
	}
	
	public int getDocCount() {
		return this.docCount;
	}
	
	public void search(Query query, AHitCollector collector) throws IOException {
		checkFlush();
		this.rlock.lock();
		try {
			final IndexSearcher searcher = new IndexSearcher(this.reader);
			collector.init(searcher);
			try {
				searcher.search(query, collector);
			} finally {
				collector.reset();
			}
		} finally {
			this.rlock.unlock();
		}
	}
	
	public void write(final Document document, final Analyzer analyzer) throws IOException, CorruptIndexException {
		wlock.lock();
		try {
			String location = document.getField(IIndexerDocument.LOCATION.getName()).stringValue();
			Term term = CACHED_LOCATION_TERM.createTerm(location);
			writer.updateDocument(term, document, analyzer);
			dirty = true;
			docCount++;
		} finally { wlock.unlock(); }
	}
	
	public void write(Document document) throws IOException, CorruptIndexException {
		write(document, analyzer);
	}
	
	public void delete(Term term) throws IOException, CorruptIndexException {
		this.wlock.lock();
		try {
			this.writer.deleteDocuments(term);
			this.dirty = true;
			this.docCount--;
		} finally { this.wlock.unlock(); }
	}
	

	public void addIndexes(IndexReader[] readers) throws CorruptIndexException, IOException {
		this.writer.addIndexes(readers);
	}	
	
	/* ================================================================================
	 * Iterators
	 * ================================================================================ */
	
	/** @deprecated does not work correctly */
	public synchronized <E extends Serializable> Iterator<E> iterator(Field<E> field) throws IOException {
		return new FieldIterator<E>(new DocumentIterator(), field);
	}
	
	/** @deprecated does not work correctly */
	public synchronized <E extends Serializable> Iterator<E> iterator(Field<E> field, String contains) throws IOException {
		return new FieldIterator<E>(new DocumentIterator(contains), field);
	}
	
	/** @deprecated does not work correctly */
	public synchronized Iterator<IIndexerDocument> docIterator() throws IOException {
		return new DocumentIterator();
	}
	
	/** @deprecated does not work correctly */
	public synchronized Iterator<IIndexerDocument> docIterator(String contains) throws IOException {
		return new DocumentIterator(contains);
	}
	
	public synchronized Iterator<String> wordIterator() throws IOException {
		return new WordIterator(this.reader.terms());
	}
	
	/** @deprecated does not work correctly */
	public synchronized Iterator<String> wordIterator(String start) throws IOException {
		return new WordIterator(this.reader.terms(new Term(null, start)));
	}
	
	public synchronized Iterator<String> wordIterator(Field<? extends Serializable> field) throws IOException {
		return new FieldLimitedWordIterator(this.reader.terms(new Term(field.getName(), "")), field);
	}
	
	public synchronized Iterator<String> wordIterator(String start, Field<? extends Serializable> field) throws IOException {
		return new FieldLimitedWordIterator(this.reader.terms(new Term(field.getName(), start)), field);
	}
	
	private class DocumentIterator implements Iterator<IIndexerDocument> {
		
		private final TermDocs tenum;
		private int current = -1;
		private int next;
		
		public DocumentIterator() throws IOException {
			this(null);
		}
		
		public DocumentIterator(String word) throws IOException {
			this.tenum = (word == null) ? reader.termDocs() : reader.termDocs(new Term("*", word));
			next0();
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
				
				// getting next lucene-doc
				final Document sourceDoc = AFlushableLuceneManager.this.reader.document(this.current);
				
				// copy data
				final IIndexerDocument targetDoc = docFactory.createDocument(IIndexerDocument.class);				
				Converter.luceneDoc2IIndexerDoc(sourceDoc,targetDoc);
				
				// return next
				return targetDoc;
			} catch (IOException e) {
				throw new RuntimeException("I/O error iterating through documents", e);
			} catch (ParseException e) {
				throw new RuntimeException("Converter error iterating through documents", e);
			}
		}
		
		public void remove() {
			throw new UnsupportedOperationException();	// index reader is read-only
			/*
			try {
				
					AFlushableLuceneManager.this.reader.deleteDocument(this.current);
				
			} catch (IOException e) {
				throw new RuntimeException("I/O error removing document", e);
			}*/
		}
	}
	
	private static class FieldIterator<E extends Serializable> implements Iterator<E> {
		
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
	
	private class WordIterator implements Iterator<String> {
		
		protected final TermEnum tenum;
		protected Term current;
		protected Term next;
		
		public WordIterator(TermEnum tenum) {
			this.tenum = tenum;
			next0();
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
				String text = this.current.text();
				this.current = next0();
				return text;
			}
		}
		
		public void remove() {
			// XXX: is urgently needed, but I don't know how to do currently
			throw new UnsupportedOperationException();
		}
	}
	
	private class FieldLimitedWordIterator extends WordIterator implements Iterator<String> {
		
		protected Field<?> field = null;
		
		public FieldLimitedWordIterator(TermEnum tenum, Field<?> field) {
			super(tenum);
			this.field = field;
		}
		
		@Override
		protected Term next0() {
			Term ret;
			do {
				ret = super.next0();
			} while (this.field != null && ret != null && !this.field.getName().equals(ret.field()));
			return ret;
		}
	}
	
	private class FlushTimerTask extends TimerTask {
		private final Log logger = LogFactory.getLog(this.getClass());
		
		@Override
		public void run() {
			try {
				checkFlush();
				this.logger.info("Index auto-flush executed successfully.");
			} catch (IOException e) {
				this.logger.error(String.format(
						"Unexpected '%s' while trying to flush the index to disk.",
						e.getClass().getName()
				),e);
			}			
		}
		
	}
}
