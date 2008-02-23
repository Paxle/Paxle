package org.paxle.indexer.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;

public class IndexerWorker extends AWorker<ICommand> {
	
	private final Log logger = LogFactory.getLog(IndexerWorker.class);
	
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
			
			this.logger.info("Indexing of URL '" + command.getLocation() + "' (MIME type '" + command.getCrawlerDocument().getMimeType() + "')");
			
			// generate the "main" indexer document from the "main" parser document including the
			// data from the command object
			indexerDoc = this.generateIIndexerDoc(
					command.getLocation(),
					command.getCrawlerDocument().getCrawlerDate(),
					null,
					command.getParserDocument()
			);
			
			// generate indexer docs from all parser-sub-documents and add them to the command
			indexerSubDocs = new ArrayList<IIndexerDocument>();
			for (Map.Entry<String,IParserDocument> pdoce : command.getParserDocument().getSubDocs().entrySet()) {
				IIndexerDocument indexerSubDoc = this.generateIIndexerDoc(
						command.getLocation(),
						command.getCrawlerDocument().getCrawlerDate(),
						pdoce.getKey(),
						pdoce.getValue()
				);
				indexerSubDocs.add(indexerSubDoc);
			}
			
			/* ================================================================
			 * Process indexer response
			 * ================================================================ */			
			
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

			// XXX: what to take if both (pdoc and cdoc) contain a different value for last mod?
			if (command.getCrawlerDocument().getLastModDate() != null) {
				indexerDoc.set(IIndexerDocument.LAST_MODIFIED, command.getCrawlerDocument().getLastModDate());
			}
			indexerDoc.set(IIndexerDocument.SIZE, command.getCrawlerDocument().getSize());
			
			// setting command status to passed
			command.setResult(ICommand.Result.Passed);
			
		} catch (Exception e) {
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
			
			this.logger.info(String.format(
					"Finished indexing of resource '%s' in %d ms.\r\n" +
					"\tCrawler-Status: '%s' %s\r\n" +
					"\tParser-Status:  '%s' %s\r\n" +
					"\tIndexer-Status: '%s' %s",					
					command.getLocation(),
					System.currentTimeMillis() - start,
					(crawlerDoc == null) ? "unknown" : crawlerDoc.getStatus().toString(),
					(crawlerDoc == null) ? "" : (crawlerDoc.getStatusText()==null)?"":crawlerDoc.getStatusText(),
					(parserDoc == null)  ? "unknown" : parserDoc.getStatus().toString(),
					(parserDoc == null)  ? "" : (parserDoc.getStatusText()==null)?"":parserDoc.getStatusText(),									
					(indexerDoc == null) ? "unknown" : indexerDoc.getStatus().toString(),
					(indexerDoc == null) ? "" : (indexerDoc.getStatusText()==null)?"":indexerDoc.getStatusText()
			));
		}
	}
	
	private IIndexerDocument generateIIndexerDoc(
			final String location,
			final Date lastCrawled,
			final String name,
			final IParserDocument pdoc) {
		final IIndexerDocument idoc = new IndexerDocument();
		try {
			final Collection<String> kw = pdoc.getKeywords();
			final Set<String> langs = pdoc.getLanguages();
			
			int idx = location.indexOf("://");
			String protocol = (idx == -1) ? null : location.substring(0, idx);
			
			/* this non-standard format has been chosen intentionally to allow an easy overview about which fields
			 * are set
			 *       Precondition                           Field-name                        Data
			 *       ~~~~~~~~~~~~                           ~~~~~~~~~~                        ~~~~
			 */
			if (pdoc.getAuthor() != null)      idoc.set(IIndexerDocument.AUTHOR,        pdoc.getAuthor());
			if (name != null)                  idoc.set(IIndexerDocument.INTERNAL_NAME, name);
			if (kw.size() > 0)                 idoc.set(IIndexerDocument.KEYWORDS,      kw.toArray(new String[kw.size()]));
			if (langs.size() > 0)              idoc.set(IIndexerDocument.LANGUAGES,     toLanguages(langs));
			                                   idoc.set(IIndexerDocument.LAST_CRAWLED,  (lastCrawled == null) ? new Date(System.currentTimeMillis()) : lastCrawled);
			if (pdoc.getLastChanged() != null) idoc.set(IIndexerDocument.LAST_MODIFIED, pdoc.getLastChanged());
			                                   idoc.set(IIndexerDocument.LOCATION,      location);
		                                       idoc.set(IIndexerDocument.MIME_TYPE,     pdoc.getMimeType());
			if (protocol != null)              idoc.set(IIndexerDocument.PROTOCOL,      protocol);
			if (pdoc.getSummary() != null)     idoc.set(IIndexerDocument.SUMMARY,       pdoc.getSummary());
			                                   idoc.set(IIndexerDocument.TEXT,          pdoc.getTextFile());
			if (pdoc.getTitle() != null)       idoc.set(IIndexerDocument.TITLE,         pdoc.getTitle());
			// TODO: IIndexerDocument.TOPICS
			
			idoc.setStatus(IIndexerDocument.Status.OK);
		} catch (Exception e) {
			this.logger.info("Unable to index the sub-document '" + name + "' of '" + location + "': " + e.getMessage(), e);
			idoc.setStatus((e instanceof IOException) ? IIndexerDocument.Status.IOError : IIndexerDocument.Status.IndexerError, e.getMessage());
		}
		return idoc;
	}
	
	private static IIndexerDocument.Language[] toLanguages(Set<String> langs) {
		final Set<IIndexerDocument.Language> result = new HashSet<IIndexerDocument.Language>();
		final Iterator<String> it = langs.iterator();
		while (it.hasNext()) {
			final String lng = it.next();
			if (lng.length() < 2)
				continue;
			if (lng.length() == 2 || lng.charAt(2) == '.')
				result.add(IIndexerDocument.Language.valueOf(lng.substring(0, 2).toLowerCase()));
		}
		return result.toArray(new IIndexerDocument.Language[result.size()]);
	}
}