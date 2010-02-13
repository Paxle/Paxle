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

package org.paxle.core.doc.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;

@Component(immediate=true, metatype=false)
@Service(IFilter.class)
@FilterTarget({
	@FilterQueuePosition(
			queueId = FilterQueuePosition.CRAWLER_IN, 
			position = Integer.MIN_VALUE,
			enabled = false
	),
	@FilterQueuePosition(
			queueId = FilterQueuePosition.CRAWLER_OUT, 
			position = Integer.MAX_VALUE,
			enabled = false
	),
	@FilterQueuePosition(
			queueId = FilterQueuePosition.PARSER_OUT, 
			position = Integer.MAX_VALUE,
			enabled = false
	),
	@FilterQueuePosition(
			queueId = FilterQueuePosition.INDEXER_OUT, 
			position = Integer.MAX_VALUE,
			enabled = false
	)
})
public class CommandDumperFilter implements IFilter<ICommand> {
	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * The {@link IDocumentFactory} required for document marshalling.
	 */
	@Reference(target="(docType=org.paxle.core.doc.ICommand)")
	protected IDocumentFactory documentFactory;
	
	public void filter(ICommand command, IFilterContext filterContext) {
		if (this.logger.isInfoEnabled()) {
			try {
				this.documentFactory.marshal(command, System.out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
