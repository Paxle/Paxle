
package org.paxle.se.index.lucene.impl;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;

import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneWriter;

public class LuceneWriter extends Thread implements ILuceneWriter, IDataConsumer<ICommand> {
	
	/**
	 * A {@link IDataSource data-source} to read {@link ICommand commands}
	 */
	private IDataSource<ICommand> source = null;
	
	/**
	 * The local logger
	 */
	private Log logger = null;
	
	private AFlushableLuceneManager manager = null;
	
	public LuceneWriter(AFlushableLuceneManager manager) {
		this.manager = manager;
		this.setName("LuceneWriter");
		this.start();
		this.logger = LogFactory.getLog(LuceneWriter.class);
		this.logger.info("Lucene writer has been started");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.core.data.IDataConsumer#setDataSource(org.paxle.core.data.IDataSource)
	 */
	public synchronized void setDataSource(IDataSource<ICommand> dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		this.source = dataSource;
		this.notify();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
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
				
				// check status
				if (command.getResult() != ICommand.Result.Passed) {
					this.logger.warn("Won't save document " + command.getLocation() + " with result '" + command.getResult() + "' (" + command.getResultText() + ")");
					continue;
				}
				
				// loop through the indexer docs
				for (IIndexerDocument indexerDoc : command.getIndexerDocuments()) {
					if (indexerDoc.getStatus() == IIndexerDocument.Status.OK) try {
						// write indexer-doc to the index
						this.write(indexerDoc);
					} catch (IndexException e) {
						this.logger.error("Error adding document to index: " + e.getMessage(), e);
						e.printStackTrace();
						indexerDoc.setStatus(IIndexerDocument.Status.IndexError, e.getMessage());
					} catch (IOException e) {
						this.logger.error("Low-level I/O error occured during adding document to index: " + e.getMessage(), e);
						e.printStackTrace();
						indexerDoc.setStatus(IIndexerDocument.Status.IOError, e.getMessage());
					} catch (Exception e) {
						this.logger.error("Internal error processing the indexer document", e);
						e.printStackTrace();
						indexerDoc.setStatus(IIndexerDocument.Status.IndexError, "Unexpected runtime exception processing the indexer document: " + e.getMessage());
					} else {
						this.logger.warn("Won't add indexer document to index with status '" + indexerDoc.getStatus() + "' (" + indexerDoc.getStatusText() + ")");
					}
				}
			}
		} catch (InterruptedException e) {
			this.logger.info("Lucene writer was interrupted, quitting...");
		} catch (Exception e) {
			this.logger.error("Internal error in lucene writer thread", e);
			e.printStackTrace();
		} 
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.paxle.se.index.IIndexWriter#write(org.paxle.core.doc.IIndexerDocument)
	 */
	public synchronized void write(IIndexerDocument document) throws IOException, IndexException {
		this.logger.debug("Adding document to index: " + document.get(IIndexerDocument.LOCATION));
		try {
			final long time = System.currentTimeMillis();
			final Document doc = Converter.iindexerDoc2LuceneDoc(document);
			final Fieldable f = doc.getField(IIndexerDocument.TEXT.getName());
			this.manager.write(doc);
			if (logger.isInfoEnabled()) {
				final Counting ct = (f == null) ? null : (PaxleTokenizer)f.tokenStreamValue();
				logger.info(String.format("Added document '%s' in %d ms to index, word-count: %s",
						document.get(IIndexerDocument.LOCATION),
						Long.valueOf(System.currentTimeMillis() - time),
						(ct == null) ? ("unknown, " + ((f == null) ? "f" : "ct") + " == null") : Integer.toString(ct.getTokenCount())));
			}
		} catch (CorruptIndexException e) {
			throw new IndexException("error adding lucene document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
		} finally {
			// close everything now
			
			Iterator<Field<?>> iter = document.fieldIterator();
			while (iter.hasNext()) {
				Field<?> key = iter.next();
				Object value = document.get(key);
				if (Closeable.class.isAssignableFrom(key.getType())) {
					((Closeable)value).close();
				}
			}
		}
	}
	
    public void delete(String location) throws IOException, IndexException {
//        this.logger.debug("Adding document to index: " + document.get(IIndexerDocument.LOCATION));
        try {
            Term term = new Term(IIndexerDocument.LOCATION.getName(),location);
            this.manager.delete(term);
        } catch (CorruptIndexException e) {
//            throw new IndexException("error deleting lucene document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
        }
    }
    
	public void close() throws IOException {
		this.interrupt();
		try { this.join(); } catch (InterruptedException e) { /* ignore this */ }
	}
}
