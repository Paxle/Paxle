/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.filter.wordlistcreator.impl;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.paxle.core.doc.ICommand;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.filter.wordlistcreator.ITokenManager;

@Component(immediate=true, metatype=true, label="FilterWordlistCreator")
@Service(IFilter.class)
@FilterTarget(@FilterQueuePosition(
		queueId = FilterQueuePosition.PARSER_OUT, 
		position = 200,
		enabled = true
))
public class WordlistCreatorFilter implements IFilter<ICommand> {

	private Log logger = LogFactory.getLog(this.getClass());

	@Reference
	protected ITokenManager tm = null;

	/**
	 * This function is called by the OSGi framework if this component is activated
	 */
	protected void activate(Map<String, Object> props)  {
	}

	protected void deactivate(){
	}

	synchronized public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");
		if (command.getResult() != ICommand.Result.Passed) return;

		try {
			long start = System.currentTimeMillis();
			this.tm.registerContent(command);
			long stop = System.currentTimeMillis();
			logger.debug("Filtering in WordlistFilter took " + (stop - start) +"ms");
		} catch (IOException e) {
			logger.warn("Exception", e);
		}
	}
}
