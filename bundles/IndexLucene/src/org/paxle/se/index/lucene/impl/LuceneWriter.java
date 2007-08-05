package org.paxle.se.index.lucene.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneWriter;

public class LuceneWriter extends IndexWriter implements ILuceneWriter, IDataConsumer<ICommand>, Runnable {
	/**
	 * A {@link IDataSource data-source} to read {@link ICommand commands}
	 */
	private IDataSource<ICommand> source = null;
	
	/**
	 * The writer thread
	 */
	private Thread writerThread = null;
	
	public static LuceneWriter createWriter(String dbpath) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		return new LuceneWriter(dbpath, new StandardAnalyzer());
	}
	
	/** @see IndexWriter#IndexWriter(String, Analyzer) */
	public LuceneWriter(String arg0, Analyzer arg1) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1);
	}
	
	/** @see IndexWriter#IndexWriter(File, Analyzer) */
	public LuceneWriter(File arg0, Analyzer arg1) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, Analyzer) */
	public LuceneWriter(Directory arg0, Analyzer arg1) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1);
	}
	
	/** @see IndexWriter#IndexWriter(String, Analyzer, boolean) */
	public LuceneWriter(String arg0, Analyzer arg1, boolean arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(File, Analyzer, boolean) */
	public LuceneWriter(File arg0, Analyzer arg1, boolean arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, Analyzer, boolean) */
	public LuceneWriter(Directory arg0, Analyzer arg1, boolean arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer) */
	public LuceneWriter(Directory arg0, boolean arg1, Analyzer arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer, boolean) */
	public LuceneWriter(
			Directory arg0,
			boolean arg1,
			Analyzer arg2,
			boolean arg3) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2, arg3);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer, IndexDeletionPolicy) */
	public LuceneWriter(
			Directory arg0,
			boolean arg1,
			Analyzer arg2,
			IndexDeletionPolicy arg3) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2, arg3);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer, boolean, IndexDeletionPolicy) */
	public LuceneWriter(
			Directory arg0,
			boolean arg1,
			Analyzer arg2,
			boolean arg3,
			IndexDeletionPolicy arg4) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2, arg3, arg4);
	}
	
	public synchronized void write(IIndexerDocument document) throws IOException, IndexException {
		try {
			super.addDocument(Converter.iindexerDoc2LuceneDoc(document));
		} catch (CorruptIndexException e) {
			throw new IndexException("error adding lucene document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
		} finally {
			// close everything now
			for (final Map.Entry<org.paxle.core.doc.Field<?>,Object> entry : document)
				if (Closeable.class.isAssignableFrom(entry.getKey().getType()))
					((Closeable)entry.getValue()).close();
		}
	}

	/**
	 * @see IDataConsumer#setDataSource(IDataSource)
	 */
	public void setDataSource(IDataSource<ICommand> dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		this.source = dataSource;
		this.notify();
	}

	/**
	 * Function to start the writer thread
	 */
	public void start() {
		if (this.writerThread != null) throw new IllegalStateException("Worker-thread already started");
		
		// wrap the writer into a thread
		this.writerThread = new Thread(this);
		
		// start it
		this.writerThread.start();
	}
	
	/**
	 * Function to stop the writer thread
	 * @throws InterruptedException
	 */
	public void stop() throws InterruptedException {
		if (this.writerThread == null) return;
		this.writerThread.interrupt();
		this.writerThread.join();
	}
	
	/**
	 * @see Runnable#run()
	 */
	public void run() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			synchronized (this) {
				while (this.source == null) this.wait();
			}

			while (!Thread.interrupted()) {
				// fetch the next command from the data-source
				ICommand command = this.source.getData();
				
				// TODO: errorhandling needed, check status
				
				// loop through the indexer docs
				for (IIndexerDocument indexerDoc : command.getIndexerDocuments()) {
					// write indexer-doc to the index
					this.write(indexerDoc);
				}
			}
		} catch (Exception e) {
			e.getStackTrace();
		} 
	}
}
