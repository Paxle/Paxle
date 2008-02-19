package org.paxle.se.index.xapian.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.IndexException;
import org.xapian.Document;
import org.xapian.WritableDatabase;
import org.xapian.Xapian;

/** http://xapian.org/docs/quickstart.html */
public class XapianWriter extends Thread implements IDataConsumer<ICommand> {

	/**
	 * A {@link IDataSource data-source} to read {@link ICommand commands}
	 */
	private IDataSource<ICommand> source = null;
	
	/**
	 * The local logger
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The xapian-database
	 */
	private WritableDatabase db = null; 
	
	/**
	 * Data directory of the {@link #db database}
	 */
	private String dbPath = null;
	
	private final IFieldManager fieldManager;		
	
	/**
	 * @param fieldManager
	 * @param databasePath
	 * @throws ExceptionInInitializerError if the database could not be created or opened properly
	 */
	public XapianWriter(IFieldManager fieldManager, String databasePath) throws ExceptionInInitializerError {
		if (fieldManager == null) throw new NullPointerException("The field-manager is null");
		
		this.fieldManager = fieldManager;
		this.dbPath = databasePath;
		this.db = this.initDatabase(databasePath);
		
		this.setName("XapianWriter " + databasePath);
		this.start();
		this.logger.info("Xapian writer has been started");		
	}	
	
	/**
	 * Creates or openes a xapian database at the specified path
	 * @param databasePath the directory where the database is located or should be created
	 * @return the opened {@link WritableDatabase database}
	 * 
	 * @throws ExceptionInInitializerError if the database could not be created or opened properly
	 */
	private WritableDatabase initDatabase(String databasePath) throws ExceptionInInitializerError {
		File databasePathFile = new File(databasePath);
		try {			
			this.logger.debug(String.format(
					"%s database '%s' ...",
					databasePathFile.exists()?"Opening":"Creating",
					databasePath
			));
			return new WritableDatabase(databasePath, Xapian.DB_CREATE_OR_OPEN);
		} catch (Throwable e) {
			String errorMsg = String.format(
					"Unexpected '%s' while %s database '%s': %s",
					e.getClass().getName(),
					databasePathFile.exists()?"opening":"creating",
					databasePath.toString(),
					e.getMessage()
			);
			
			// Make sure you log the exception, as it might be swallowed
			this.logger.error(errorMsg,e);
			throw new ExceptionInInitializerError(errorMsg);
		}
	}
	
	/**
	 * @see IDataConsumer#setDataSource(IDataSource)
	 */
	public void setDataSource(IDataSource<ICommand> dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		
		this.source = dataSource;
		this.logger.debug("Datasource was set");
		this.notify();
	}
	
	/**
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		try {
			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
			
			// wait until the data-source was set
			synchronized (this) {
				while (this.source == null) this.wait();
			}
			
			while (!Thread.interrupted()) {
				// fetch the next command from the data-source
				ICommand command = this.source.getData();
				
				// check status
				if (command.getResult() != ICommand.Result.Passed) {
					this.logger.warn(String.format(
							"Won't save document '%s' with result '%s' (%s)",
							command.getLocation(),
							command.getResult()==null?"unknown":command.getResult().toString(),
							command.getResultText()							
					));
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
			this.logger.info("Xapian writer was interrupted, quitting...");
		} catch (Exception e) {
			this.logger.error("Internal error in Xapian writer thread", e);
			e.printStackTrace();
		} 
	}			

	public synchronized void write(IIndexerDocument document) throws IOException, IndexException {
		this.logger.debug("Adding document to index: " + document.get(IIndexerDocument.LOCATION));
		try {
			// create an empty xapian document
			Document doc = new Document();
			
			// fill it with data
			for (final Map.Entry<org.paxle.core.doc.Field<?>,Object> entry : document) {
				Field<?> key = entry.getKey();
				Object value = entry.getValue();

				
			}
			
			this.db.addDocument(doc);		
		} catch (Exception e) {
			throw new IndexException("error adding xapian document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
		} finally {
			// close everything now
			for (final Map.Entry<Field<?>,Object> entry : document)
				if (Closeable.class.isAssignableFrom(entry.getKey().getType()))
					((Closeable)entry.getValue()).close();
		}
	}
}
