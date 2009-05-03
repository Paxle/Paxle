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
package org.paxle.indexer.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;

public class IndexerWorker extends AWorker<ICommand> {
	
	private final Log logger = LogFactory.getLog(IndexerWorker.class);
	
	private final IDocumentFactory idocFactory;
	
	public IndexerWorker(IDocumentFactory idocFactory) {
		this.idocFactory = idocFactory;
	}
	
	@Override
	protected void execute(ICommand command) {		
		final long start = System.currentTimeMillis();
		
		IIndexerDocument indexerDoc = null;
		ArrayList<IIndexerDocument> indexerSubDocs = null;
		try {
			/* ================================================================
			 * Input Parameter Check
			 * ================================================================ */
			String errorMsg = null;
			if (command.getResult() != ICommand.Result.Passed) {
				errorMsg = String.format(
						"Won't index resource '%s'. Command status is: '%s' (%s)",
						command.getLocation(),
						command.getResult(),
						command.getResultText()
				);		
			} else if (command.getCrawlerDocument() == null) {
				errorMsg = String.format(
						"Won't index resource '%s'. Crawler-document is null",
						command.getLocation()
				);
			} else if (command.getCrawlerDocument().getStatus() != ICrawlerDocument.Status.OK) {
				errorMsg = String.format(
						"Won't index resource '%s'. Crawler-document status is: '%s' (%s)",
						command.getLocation(),
						command.getCrawlerDocument().getStatus(),
						command.getCrawlerDocument().getStatusText()
				);							
			} else if (command.getParserDocument() == null) {
				errorMsg = String.format(
						"Won't index resource '%s'. Parser-document is null",
						command.getLocation()
				);
			} else if (command.getParserDocument().getStatus() != IParserDocument.Status.OK) {
				errorMsg = String.format(
						"Won't index resource '%s'. Parser-document status is: '%s' (%s)",
						command.getLocation(),
						command.getCrawlerDocument().getStatus(),
						command.getCrawlerDocument().getStatusText()
				);			
			}
			
			if (errorMsg != null) {
				this.logger.warn(errorMsg);
				return;
			}

			/* ================================================================
			 * Generate Indexer Document
			 * ================================================================ */
			
			// generate the "main" indexer document from the "main" parser document including the
			// data from the command object
			if ((command.getParserDocument().getFlags() & IParserDocument.FLAG_NOINDEX) == 0) {
				this.logger.info(String.format("Indexing of URL '%s' (%s) ...",
						command.getLocation(),
						command.getCrawlerDocument().getMimeType()));
				
				indexerDoc = this.generateIIndexerDoc(
						command.getLocation(),
						command.getCrawlerDocument().getCrawlerDate(),
						null,
						command.getParserDocument()
				);
			} else {
				this.logger.info(String.format("Indexing of URL '%s' (%s) ommitted due to 'noindex'-flag",
						command.getLocation(),
						command.getCrawlerDocument().getMimeType()));
				
				// don't exit here already, we still have to process the sub-parser-docs
			}
			
			// generate indexer docs from all parser-sub-documents and add them to the command
			indexerSubDocs = new ArrayList<IIndexerDocument>();
			
			final class Entry {
				public String key;
				public IParserDocument pdoc;
				public Entry(final String key, final IParserDocument pdoc) {
					this.key = key;
					this.pdoc = pdoc;
				}
			}
			
			// traverse the tree of sub-documents
			final Queue<Entry> queue = new LinkedList<Entry>();
			for (Map.Entry<String,IParserDocument> pdoce : command.getParserDocument().getSubDocs().entrySet())
				queue.add(new Entry(pdoce.getKey(), pdoce.getValue()));
			
			while (!queue.isEmpty()) {
				Entry e = queue.remove();
				if ((e.pdoc.getFlags() & IParserDocument.FLAG_NOINDEX) == 0) {
					IIndexerDocument indexerSubDoc = this.generateIIndexerDoc(
							command.getLocation(),
							command.getCrawlerDocument().getCrawlerDate(),
							e.key,
							e.pdoc
					);
					indexerSubDocs.add(indexerSubDoc);
				}
				
				for (final Map.Entry<String,IParserDocument> pdoce : e.pdoc.getSubDocs().entrySet())
					queue.add(new Entry(e.key + "/" + pdoce.getKey(), pdoce.getValue()));
			}
			
			/* ================================================================
			 * Process indexer response
			 * ================================================================ */			
			
			/* There may be the case, that - i.e. by a document's and it's parser's restriction - the main
			 * document, from which the sub-docs are retrieved, may not be indexed, but links, and therefore
			 * sub-docs, may be followed.
			 * In this case we simply omit the main document. If the document has no children, then this is the
			 * only thing we need to check for correctness. */
			if (indexerSubDocs.size() == 0) {
				
				if (indexerDoc == null) {
					command.setResult(
							ICommand.Result.Failure, 
							String.format("Indexer returned no indexer-document.")
					);
					return;
				} else if (indexerDoc.getStatus() == null || indexerDoc.getStatus() != IIndexerDocument.Status.OK) {
					command.setResult(
							ICommand.Result.Failure, 
							String.format("Indexer-document status is '%s'.",indexerDoc.getStatus())
					);
					return;
				}
				
			}

			// XXX: what to take if both (pdoc and cdoc) contain a different value for last mod?
			if (command.getCrawlerDocument().getLastModDate() != null) {
				indexerDoc.set(IIndexerDocument.LAST_MODIFIED, command.getCrawlerDocument().getLastModDate());
			}
			indexerDoc.set(IIndexerDocument.SIZE, Long.valueOf(command.getCrawlerDocument().getSize()));
			
			// setting command status to passed
			command.setResult(ICommand.Result.Passed);
			
		} catch (Throwable e) {
			// setting command status
			command.setResult(
					ICommand.Result.Failure, 
					String.format("Unexpected '%s' while indexing resource. %s",e.getClass().getName(),e.getMessage())
			);
			
			// log error
			this.logger.warn(String.format("Unexpected '%s' while indexing resource '%s'.",
					e.getClass().getName(),
					command.getLocation()
			),e);						
		} finally {
			/* Add indexer-docs to command-object.
			 * 
			 * This must be done even in error situations to 
			 * - allow filters to correct the error (if possible)
			 * - to report the error back properly (e.g. to store it into db
			 *   or send it back to a remote peer). 
			 */
			if (indexerDoc != null) {
				command.addIndexerDocument(indexerDoc);
			}

			if (indexerSubDocs != null) {
				// get all indexer-sub-docs and add them to the command
				for (IIndexerDocument indexerSubDoc : indexerSubDocs) {
					// XXX: do sub-docs need a size-field, too?
					command.addIndexerDocument(indexerSubDoc);
				}
			}
			
			ICrawlerDocument crawlerDoc = command.getCrawlerDocument();
			IParserDocument parserDoc = command.getParserDocument();
			
			if (logger.isDebugEnabled()) {
				this.logger.info(String.format(
						"Finished indexing of resource '%s' in %d ms.\r\n" +
						"\tCrawler-Status: '%s' %s\r\n" +
						"\tParser-Status:  '%s' %s\r\n" +
						"\tIndexer-Status: '%s' %s",					
						command.getLocation(),
						Long.valueOf(System.currentTimeMillis() - start),
						(crawlerDoc == null) ? "unknown" : crawlerDoc.getStatus().toString(),
						(crawlerDoc == null) ? "" : (crawlerDoc.getStatusText()==null)?"":crawlerDoc.getStatusText(),
						(parserDoc == null)  ? "unknown" : parserDoc.getStatus().toString(),
						(parserDoc == null)  ? "" : (parserDoc.getStatusText()==null)?"":parserDoc.getStatusText(),									
						(indexerDoc == null) ? "unknown" : indexerDoc.getStatus().toString(),
						(indexerDoc == null) ? "" : (indexerDoc.getStatusText()==null)?"":indexerDoc.getStatusText()
				));
			} else if (logger.isInfoEnabled()) {
				this.logger.info(String.format(
						"Finished indexing of resource '%s' in %d ms.\r\n" +
						"\tIndexer-Status: '%s' %s",					
						command.getLocation(),
						Long.valueOf(System.currentTimeMillis() - start),									
						(indexerDoc == null) ? "unknown" : indexerDoc.getStatus().toString(),
						(indexerDoc == null) ? "" : (indexerDoc.getStatusText()==null)?"":indexerDoc.getStatusText()));
			}
		}
	}
	
	private IIndexerDocument generateIIndexerDoc(
			final URI location,
			final Date lastCrawled,
			final String name,
			final IParserDocument pdoc) throws IOException {
		final IIndexerDocument idoc = this.idocFactory.createDocument(IIndexerDocument.class);
		try {
			final Collection<String> kw = pdoc.getKeywords();
			final Set<String> lng = pdoc.getLanguages();
			
			String protocol = location.getScheme();
			
			/* this non-standard format has been chosen intentionally to allow an easy overview about which fields
			 * are set
			 *       Precondition                           Field-name                        Data
			 *       ~~~~~~~~~~~~                           ~~~~~~~~~~                        ~~~~
			 */
			if (pdoc.getAuthor() != null)      idoc.set(IIndexerDocument.AUTHOR,        pdoc.getAuthor());
			if (name != null)                  idoc.set(IIndexerDocument.INTERNAL_NAME, name);
			if (kw.size() > 0)                 idoc.set(IIndexerDocument.KEYWORDS,      kw.toArray(new String[kw.size()]));
			if (lng != null && lng.size() > 0) idoc.set(IIndexerDocument.LANGUAGES,     lng.toArray(new String[lng.size()]));
			                                   idoc.set(IIndexerDocument.LAST_CRAWLED,  (lastCrawled == null) ? new Date(System.currentTimeMillis()) : lastCrawled);
			if (pdoc.getLastChanged() != null) idoc.set(IIndexerDocument.LAST_MODIFIED, pdoc.getLastChanged());
			                                   idoc.set(IIndexerDocument.LOCATION,      location.toString());
		                                       idoc.set(IIndexerDocument.MIME_TYPE,     pdoc.getMimeType());
			if (protocol != null)              idoc.set(IIndexerDocument.PROTOCOL,      protocol);
			if (pdoc.getSummary() != null)     idoc.set(IIndexerDocument.SUMMARY,       pdoc.getSummary());
			if (pdoc.getTextFile() != null)    idoc.set(IIndexerDocument.TEXT,          pdoc.getTextFile());
			if (pdoc.getTitle() != null)       idoc.set(IIndexerDocument.TITLE,         pdoc.getTitle());
			// TODO: IIndexerDocument.TOPICS
			
			idoc.setStatus(IIndexerDocument.Status.OK);
		} catch (Exception e) {
			this.logger.info("Unable to index the sub-document '" + name + "' of '" + location + "': " + e.getMessage(), e);
			idoc.setStatus((e instanceof IOException) ? IIndexerDocument.Status.IOError : IIndexerDocument.Status.IndexerError, e.getMessage());
		}
		return idoc;
	}
}
