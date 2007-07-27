package org.paxle.indexer.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;

import org.paxle.indexer.IndexerDocument;

public class IndexerWorker extends AWorker {
	
	@Override
	protected void execute(ICommand cmd) {
		final IIndexerDocument idoc = new IndexerDocument();
		final Collection<String> kw = cmd.getParserDocument().getKeywords();
		try {
			idoc.set(IIndexerDocument.AUTHOR, 			cmd.getParserDocument().getAuthor());
			idoc.set(IIndexerDocument.KEYWORDS, 		kw.toArray(new String[kw.size()]));
			idoc.set(IIndexerDocument.LANGUAGES, 		toLanguages(cmd.getParserDocument().getLanguages()));
			idoc.set(IIndexerDocument.LAST_CRAWLED, 	cmd.getCrawlerDocument().getCrawlerDate());
			idoc.set(IIndexerDocument.LAST_MODIFIED, 	cmd.getCrawlerDocument().getLastModDate());
			idoc.set(IIndexerDocument.LOCATION, 		cmd.getLocation());
			idoc.set(IIndexerDocument.SIZE, 			cmd.getCrawlerDocument().getSize());
			idoc.set(IIndexerDocument.SUMMARY, 			cmd.getParserDocument().getSummary());
			idoc.set(IIndexerDocument.TEXT, 			cmd.getParserDocument().getTextAsReader());
			idoc.set(IIndexerDocument.TITLE, 			cmd.getParserDocument().getTitle());
			// IIndexerDocument.TOPICS
		
			cmd.setIndexerDocument(idoc);
		} catch (IOException e) {
			cmd.setResult(ICommand.Result.Failure, e.getMessage());
		}
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
