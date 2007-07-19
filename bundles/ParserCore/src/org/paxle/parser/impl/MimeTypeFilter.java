package org.paxle.parser.impl;

import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.ICommand;
import org.paxle.parser.ISubParser;

/**
 * Filters {@link ICommand commands} out if the mime-type of the
 * resource is not supported by one of the available {@link ISubParser sub-parsers}
 */
public class MimeTypeFilter implements IFilter<ICommand> {
	private SubParserManager subParserManager = null;
	
	public MimeTypeFilter(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}

	public void filter(ICommand command) {
		// TODO: get the mimetype of the crawled resource
		String mimeType = null;
		
		// check if the mime-type is supported by one of the 
		// available sub-parsers
		if (!this.subParserManager.isSupported(mimeType)) {
			// TODO: set the statuscode of the command accordingly
		}		
		
	}
}
