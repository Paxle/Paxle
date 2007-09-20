package org.paxle.indexer.impl;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.IndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;

public class IndexerWorker extends AWorker<ICommand> {
	
	private final Log logger = LogFactory.getLog(IndexerWorker.class);
	
	@Override
	protected void execute(ICommand cmd) {
		if (cmd.getResult() != ICommand.Result.Passed) {
			this.logger.warn("Won't index document " + cmd.getLocation() + " with result '" + cmd.getResult() + "' (" + cmd.getResultText() + ")");
			return;
		}

		try {
			final IIndexerDocument idoc;
			this.logger.info("Indexing of URL '" + cmd.getLocation() + "' (MIME type '" + cmd.getCrawlerDocument().getMimeType() + "')");
			// generate the "main" indexer document from the "main" parser document including the
			// data from the command object
			idoc = generateIIndexerDoc(
					cmd.getLocation(),
					cmd.getCrawlerDocument().getCrawlerDate(),
					null,
					cmd.getParserDocument());
			if (idoc.getStatus() != IIndexerDocument.Status.OK) {
				cmd.setResult(ICommand.Result.Failure, idoc.getStatusText());
				return;
			}

			// XXX: what to take if both (pdoc and cdoc) contain a different value for last mod?
			if (cmd.getCrawlerDocument().getLastModDate() != null)
				idoc.set(IIndexerDocument.LAST_MODIFIED, cmd.getCrawlerDocument().getLastModDate()); 
			idoc.set(IIndexerDocument.SIZE, cmd.getCrawlerDocument().getSize());
			idoc.setStatus(IIndexerDocument.Status.OK);
			cmd.addIndexerDocument(idoc);

			// generate indexer docs from all parser-sub-documents and add them to the command
			for (Map.Entry<String,IParserDocument> pdoce : cmd.getParserDocument().getSubDocs().entrySet()) {
				// XXX: do sub-docs need a size-field, too?
				cmd.addIndexerDocument(generateIIndexerDoc(
						cmd.getLocation(),
						cmd.getCrawlerDocument().getCrawlerDate(),
						pdoce.getKey(),
						pdoce.getValue()));
			}
			cmd.setResult(ICommand.Result.Passed);
		} catch (Exception e) {
			cmd.setResult(ICommand.Result.Failure, "Unexpected error while indexing the resource");
			e.printStackTrace();
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
			                                   idoc.set(IIndexerDocument.PROTOCOL,      new URL(location).getProtocol());
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
