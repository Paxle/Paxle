package org.paxle.data.db.impl;

import java.util.Map;
import java.util.Set;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.Command;
import org.paxle.core.queue.ICommand;

public class UrlExtractorFilter implements IFilter<ICommand> {
	private CommandDB db;

	public UrlExtractorFilter(CommandDB db) {
		this.db = db;
	}

	@SuppressWarnings("unchecked")
	public void filter(ICommand command) {
		if (command == null) return;

		// getting the parser-doc
		IParserDocument parserDoc = command.getParserDocument();
		if (parserDoc == null) return;

		// getting the link map
		Map linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			for (String ref : (Set<String>) linkMap.keySet()) {
				// do some url normalization here
				// TODO: this should be done in another filter
				int idx = ref.indexOf("#");
				if (idx != -1) ref = ref.substring(0,idx);
				
				if (!db.isKnown(ref)) {
					db.storeCommand(Command.createCommand(ref));
				}
			}
		}
	}

}
