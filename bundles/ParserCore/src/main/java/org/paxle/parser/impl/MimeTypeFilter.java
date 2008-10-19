/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.parser.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

/**
 * Filters {@link ICommand commands} out if the mime-type of the
 * resource is not supported by one of the available {@link ISubParser sub-parsers}
 */
public class MimeTypeFilter implements IFilter<ICommand> {
	private Log logger = LogFactory.getLog(this.getClass());
	private ISubParserManager subParserManager = null;
	
	public MimeTypeFilter(ISubParserManager subParserManager) {
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
