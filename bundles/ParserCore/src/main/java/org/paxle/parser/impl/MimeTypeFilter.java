/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.parser.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.parser.ISubParser;
import org.paxle.parser.ISubParserManager;

/**
 * Filters {@link ICommand commands} out if the mime-type of the
 * resource is not supported by one of the available {@link ISubParser sub-parsers}
 */
@Component(metatype=false, immediate=true)
@Service(IFilter.class)
@Properties({
	@Property(name="org.paxle.metadata", boolValue=true),
	@Property(name="org.paxle.metadata.localization", value="/OSGI-INF/l10n/MimeTypeFilter")
})
@FilterTarget({
	@FilterQueuePosition(queueId=FilterQueuePosition.PARSER_IN)
})
public class MimeTypeFilter implements IFilter<ICommand> {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A component to manage all {@link ISubParser}s installed on the system
	 */
	@Reference
	protected ISubParserManager subParserManager;
	
	public void filter(ICommand command, IFilterContext context) {		
		final ICrawlerDocument cdoc = command.getCrawlerDocument();
		if (cdoc == null) return;
		
		// get the mimetype of the document
		final String mimeType = cdoc.getMimeType();
		
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
