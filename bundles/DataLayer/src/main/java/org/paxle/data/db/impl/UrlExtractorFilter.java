
package org.paxle.data.db.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class UrlExtractorFilter implements IFilter<ICommand> {
	
	private static class Counter {
		
		public int c = 0;
		public int known = 0;
	}
	
	private CommandDB db;
	
	private Log logger = LogFactory.getLog(this.getClass());

	public UrlExtractorFilter(CommandDB db) {
		this.db = db;
	}

	@SuppressWarnings("unchecked")
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) return;

		// getting the parser-doc
		IParserDocument parserDoc = command.getParserDocument();
		if (parserDoc == null) return;

		// getting the link map
		final Counter c = new Counter();
		this.extractLinks(command, parserDoc, c);
		logger.info(String.format("Extracted %d new and %d already known URIs from '%s'",
				Integer.valueOf(c.c - c.known), Integer.valueOf(c.known), command.getLocation()));
	}
	
	private void extractLinks(final ICommand command, IParserDocument parserDoc, final Counter c) {
		if (parserDoc == null) return;
		
		// getting the link map
		Map<URI, String> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.extractLinks(command, linkMap, c);
		}
		
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.extractLinks(command, subDoc, c);
			}
		}
	}
	
	private void extractLinks(final ICommand command, Map<URI, String> linkMap, final Counter c) {
		ArrayList<URI> locations = new ArrayList<URI>();
		
		for (URI ref : linkMap.keySet()) {
			if (ref.toString().length() > 512) {
				this.logger.debug("Skipping too long URL: " + ref);
				continue;
			}
			
			// add command into list
			locations.add(ref);
		}

		// store commands into DB
		if (!db.isClosed()) {
			c.known += db.storeUnknownLocations(
					command.getProfileOID(),
					command.getDepth()+1,
					locations
			);
			c.c += locations.size();
		} else {
			this.logger.error(String.format(
					"Unable to write linkmap of location '%s' to db. Database already closed.",
					command.getLocation().toASCIIString()
			));
		}
	}

}
