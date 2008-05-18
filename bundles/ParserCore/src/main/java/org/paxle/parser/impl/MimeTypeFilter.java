package org.paxle.parser.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandProfile;
import org.paxle.core.queue.ICommandProfileManager;
import org.paxle.parser.ISubParser;

/**
 * Filters {@link ICommand commands} out if the mime-type of the
 * resource is not supported by one of the available {@link ISubParser sub-parsers}
 */
public class MimeTypeFilter implements IFilter<ICommand> {
	private Log logger = LogFactory.getLog(this.getClass());
	private SubParserManager subParserManager = null;
	
	public MimeTypeFilter(SubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}

	public void filter(ICommand command, IFilterContext context) {
		if (command.getCrawlerDocument() == null) return;
		
		// get the mimetype of the document
		String mimeType = command.getCrawlerDocument().getMimeType();
		
		// check if the mime-type is supported by one of the 
		// available sub-parsers
		if (!this.subParserManager.isSupported(mimeType)) {
			this.logger.info(String.format("Mime-type '%s' of resource '%s' not supported.",mimeType,command.getLocation()));
			command.setResult(ICommand.Result.Rejected, String.format("MimeType '%s' not supported", mimeType));
		}		
		
		// TODO: checking if the command-profile has additional restrictions
		/*
		int profileID = command.getProfileOID();
		if (profileID >= 0) {
			ICommandProfileManager profileManager = context.getCommandProfileManager();
			if (profileManager != null) {
				ICommandProfile profile = profileManager.getProfileByID(profileID);
				if (profile != null) {
					
				}
			}
		}
		*/		
	}
}
