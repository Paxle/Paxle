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

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.doc.ICommand;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.io.IIOTools;

@Component(immediate=true, metatype=true, label="FilterWordlistCreator")
@Service(IFilter.class)
@Property(name="dataPath", value="wordlistCreator")
@FilterTarget(@FilterQueuePosition(
		queue = "org.paxle.parser.out", 
		position = 200,
		enabled = true
))
public class WordlistCreatorFilter implements IFilter<ICommand> {

	/**
	 * Path where the data should be stored
	 */
	private File dataDir;
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	@Reference
	protected IIOTools ioTools;
	
	/**
	 * This function is called by the OSGi framework if this component is activated
	 */
	protected void activate(ComponentContext context)  {
		System.out.println("######################ACTIVATED######################################");
		System.out.println("######################ACTIVATED######################################");
		logger.warn("######################ACTIVATED######################################");
		logger.warn("######################ACTIVATED######################################");
		String dataPath = (String) context.getProperties().get("dataPath");
		
		// getting the data directory to use
		this.dataDir = new File(System.getProperty("paxle.data") + File.separatorChar + dataPath);
		if (!dataDir.exists()) dataDir.mkdirs();		
	}
	
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");
		if (command.getResult() != ICommand.Result.Passed) return;
		if (command.getParserDocument() == null) return;	
	}


}
