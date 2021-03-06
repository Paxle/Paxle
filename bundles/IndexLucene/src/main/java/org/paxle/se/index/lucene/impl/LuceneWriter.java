/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Services;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandTracker;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.index.IndexException;

@Component(immediate=true, metatype=false)
@Services({
	@Service(IIndexWriter.class),
	@Service(IDataConsumer.class)
})
@Property(name = IDataConsumer.PROP_DATACONSUMER_ID, value="org.paxle.indexer.source")
public class LuceneWriter extends Thread implements IIndexWriter, IDataConsumer<ICommand> {
	
	/**
	 * A {@link IDataSource data-source} to read {@link ICommand commands}
	 */
	private IDataSource<ICommand> source = null;
	
	/**
	 * The local logger
	 */
	private Log logger = LogFactory.getLog(LuceneWriter.class);
	
	@Reference
	protected ICommandTracker commandTracker;
	
	@Reference
	protected ILuceneManager manager;
	
	@Reference
	protected IStopwordsManager stopwordsManager;
	
	protected Converter defaultCv;
	
	@Activate
	protected void activate(Map<String, Object> props) {
		this.defaultCv = new Converter(this.stopwordsManager.getDefaultAnalyzer());
	
		this.setPriority(3);
		this.setName("LuceneWriter");
		this.start();
		
		this.logger.info("Lucene writer has been started");
	}
	
	@Deactivate
	protected void deactivate() throws IOException {
		this.interrupt();
		try { this.join(); } catch (InterruptedException e) { /* ignore this */ }
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
	@Override
	public void run() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			synchronized (this) {
				while (this.source == null) this.wait();
			}

			while (!Thread.interrupted()) {
				ICommand command = null;
				
				try {
					// fetch the next command from the data-source
					command = this.source.getData();

					// check status
					if (command.getResult() != ICommand.Result.Passed) {
						this.logger.warn(String.format("Won't save document '%s' with result '%s' (%s)",
								command.getLocation(),
								command.getResult(),
								command.getResultText()));
						continue;
					}

					// loop through the indexer docs
					IIndexerDocument[] indexerDocs = command.getIndexerDocuments();
					if (indexerDocs == null) {
						this.logger.warn(String.format(
								"Won't save document '%s'. No indexer-documents available.",
								command.getLocation()
						));
						continue;						
					}
					
					for (IIndexerDocument indexerDoc : indexerDocs) {
						if (indexerDoc.getStatus() == IIndexerDocument.Status.OK) try {
							// write indexer-doc to the index
							this.write(indexerDoc);
						} catch (IndexException e) {
							this.logger.error("Error adding document to index: " + e.getMessage(), e);
							indexerDoc.setStatus(IIndexerDocument.Status.IndexError, e.getMessage());
						} catch (IOException e) {
							this.logger.error("Low-level I/O error occured during adding document to index: " + e.getMessage(), e);
							indexerDoc.setStatus(IIndexerDocument.Status.IOError, e.getMessage());
						} catch (Exception e) {
							this.logger.error("Internal error processing the indexer document", e);
							indexerDoc.setStatus(
									IIndexerDocument.Status.IndexError,
									"Unexpected runtime exception processing the indexer document: " + e.getMessage());
						} else {
							this.logger.warn(String.format("Won't save indexer-doc for location '%s' with status '%s' (%s)",
									indexerDoc.get(IIndexerDocument.LOCATION),
									indexerDoc.getStatus(),
									indexerDoc.getStatusText()));
						}
					}
				} finally {
					if (this.commandTracker != null && command != null) {
						// notify the command-tracker about the destruction of the command-event
						this.commandTracker.commandDestroyed(LuceneWriter.class.getName(), command);
					}
				}
			}
		} catch (InterruptedException e) {
			this.logger.info("Lucene writer was interrupted, quitting...");
		} catch (Throwable e) {
			this.logger.error("Internal error in lucene writer thread", e);
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
			
			final String[] langs = document.get(IIndexerDocument.LANGUAGES);
			
			// depending on whether the document has set a valid language or not, either the
			// default stop-words set of Lucene is used or - if available for the language -
			// the one provided by the stopwordsManager through an instance of PaxleAnalyzer
			// to convert the IIndexerDocument into a Document of Lucene and after that to
			// store it.
			//
			// TODO: extend the StopwordsManager to store the Converters for the languages
			//       to not always having to create a new instance
			final String lang;
			final Converter cv;
			if (langs == null || langs.length == 0) {
				lang = null;
				cv = defaultCv;
			} else {
				lang = langs[0];
				cv = new Converter(stopwordsManager.getAnalyzer(lang));
			}
			
			final int wc;
			try {
				// cv's internal counters (which are in fact token-streams) are created here
				final Document doc = cv.iindexerDoc2LuceneDoc(document);
				// the converted document is written to Lucene's cache here
				this.manager.write(doc, cv.getAnalyzer());
				
			} finally {
				// the token-streams provided by the given analyzer contained in the converter
				// are only read when manager.write() is called, so we have to get the number of
				// words afterwards.
				wc = cv.getCountersAccumulated();
				
				// since writing may fail because of whatever, it is ensured here that the counter
				// is reset, independently of the status of the write-operation
				cv.resetCounters();
			}
			
			if (logger.isInfoEnabled()) {
				final String internalName = document.get(IIndexerDocument.INTERNAL_NAME);
				logger.info(String.format(
						"Added document '%s' in %d ms to index\n" +
						"\tTitle: %s%s\n" +
						"\tsize: %d bytes, word-count: %d, language: %s, mime-type: %s",
						document.get(IIndexerDocument.LOCATION), Long.valueOf(System.currentTimeMillis() - time),
						document.get(IIndexerDocument.TITLE),
						(internalName == null || internalName.length() == 0) ? "" : " (internal: " + internalName + ")",
						document.get(IIndexerDocument.SIZE), Integer.valueOf(wc), lang, document.get(IIndexerDocument.MIME_TYPE))
				);
			}
		} catch (CorruptIndexException e) {
			throw new IndexException("error adding lucene document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
		} finally {
			
			// close everything now
			Iterator<Field<?>> iter = document.fieldIterator();
			while (iter.hasNext()) {
				final Field<?> key = iter.next();
				final Object value = document.get(key);
				
				if (Closeable.class.isAssignableFrom(key.getType())) {
					try {
						((Closeable)value).close();
					} catch (IOException e) {
						logger.error("I/O exception while closing value of field '" + key + "': " + value, e);
					}
				}
			}
		}
	}
	
	public void delete(String location) throws IOException, IndexException {
//		this.logger.debug("Adding document to index: " + document.get(IIndexerDocument.LOCATION));
		try {
			Term term = new Term(IIndexerDocument.LOCATION.getName(),location);
			this.manager.delete(term);
		} catch (CorruptIndexException e) {
//			throw new IndexException("error deleting lucene document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
		}
	}

	public void mergeIndex(String pathToIndex) {
		IndexReader [] readers = new IndexReader[1];
		try {
			final Directory dir = FSDirectory.open(new File(pathToIndex));
			readers[0] = IndexReader.open(dir, true);
			this.manager.addIndexes(readers);
		} catch (CorruptIndexException e) {
			this.logger.error(e);
		} catch (IOException e) {
			this.logger.error(e);
		}
	}
}
