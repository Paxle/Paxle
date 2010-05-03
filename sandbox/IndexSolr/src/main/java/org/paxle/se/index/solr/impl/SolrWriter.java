package org.paxle.se.index.solr.impl;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.common.SolrInputDocument;
import org.paxle.core.data.IDataConsumer;
import org.paxle.core.data.IDataSource;
import org.paxle.core.doc.Field;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IFieldManager;
import org.paxle.se.index.IIndexWriter;
import org.paxle.se.index.IndexException;

/**
 * http://wiki.apache.org/solr/Solrj
 */
public class SolrWriter extends Thread implements IDataConsumer<ICommand>, IIndexWriter {
	
	/**
	 * A {@link IDataSource data-source} to read {@link ICommand commands}
	 */
	private IDataSource<ICommand> source = null;
	
	/**
	 * The local logger
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The Solr-server used to store the data
	 */
	private SolrServer server = null;	
	
	private final IFieldManager fieldManager;	
	
	public SolrWriter(IFieldManager fieldManager, URL serverURL) throws SolrServerException, IOException {
		if (fieldManager == null) throw new NullPointerException("The field-manager is null.");
		if (serverURL == null) throw new NullPointerException("The URL to the solr-server is null.");
		
		this.fieldManager = fieldManager;
		this.server = new CommonsHttpSolrServer(serverURL);
		
		// test connection
		SolrPingResponse pingResponse = this.server.ping();
		this.logger.info(String.format(
				"Ping to Sorl server '%s' took '%d' ms.",
				serverURL.toString(),
				Long.valueOf(pingResponse.getElapsedTime())
		));		
		
//		((CommonsHttpSolrServer)server).setConnectionTimeout(100);
//		((CommonsHttpSolrServer)server).setDefaultMaxConnectionsPerHost(100);
//		((CommonsHttpSolrServer)server).setMaxTotalConnections(100);			
	}
	
	/**
	 * @see IDataConsumer#setDataSource(IDataSource)
	 */
	public synchronized void setDataSource(IDataSource<ICommand> dataSource) {
		if (dataSource == null) throw new NullPointerException("The data-source is null.");
		if (this.source != null) throw new IllegalStateException("The data-source was already set.");
		
		this.source = dataSource;
		this.notify();
	}
	
	/**
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
				final ICommand command = this.source.getData();
				
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
						indexerDoc.setStatus(IIndexerDocument.Status.IndexError, e.getMessage());
					} catch (IOException e) {
						this.logger.error("Low-level I/O error occured during adding document to index: " + e.getMessage(), e);
						indexerDoc.setStatus(IIndexerDocument.Status.IOError, e.getMessage());
					} catch (Exception e) {
						this.logger.error("Internal error processing the indexer document", e);
						indexerDoc.setStatus(IIndexerDocument.Status.IndexError, "Unexpected runtime exception processing the indexer document: " + e.getMessage());
					} else {
						this.logger.warn("Won't add indexer document to index with status '" + indexerDoc.getStatus() + "' (" + indexerDoc.getStatusText() + ")");
					}
				}
			}
		} catch (InterruptedException e) {
			this.logger.info("Solr writer was interrupted, quitting...");
		} catch (Exception e) {
			this.logger.error("Internal error in solr writer thread", e);
			e.printStackTrace();
		} 
	}
	
	/**
	 * @see org.paxle.se.index.IIndexWriter#write(org.paxle.core.doc.IIndexerDocument)
	 */
	public void write(IIndexerDocument document) throws IOException, IndexException {
		this.logger.debug("Adding document to index: " + document.get(IIndexerDocument.LOCATION));
		try {
			SolrInputDocument doc1 = new SolrInputDocument();
			
			doc1.addField( "id", "id1", 1.0f );
//		    server.add( doc1);
			
		    server.commit();						
		} catch (Exception e) {
			throw new IndexException("error adding solr document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
		} finally {
			// close everything now
			for (final Map.Entry<Field<?>,Object> entry : document)
				if (Closeable.class.isAssignableFrom(entry.getKey().getType()))
					((Closeable)entry.getValue()).close();
		}
	}

	public void delete(String arg0) throws IOException, IndexException {
		// TODO: this.server.deleteById(id);
	}

	public void close() throws IOException {
		
	}
}
