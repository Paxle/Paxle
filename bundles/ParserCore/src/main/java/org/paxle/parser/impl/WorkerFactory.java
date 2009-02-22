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
package org.paxle.parser.impl;

import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;
import org.paxle.parser.ISubParserManager;

public class WorkerFactory implements IWorkerFactory<ParserWorker> {
	
	private final ISubParserManager subParserManager;
	
	/**
	 * @param subParserManager the {@link SubParserManager} that should be passed 
	 *        to the {@link ParserWorker worker-thread} on {@link #createWorker() worker-creation}
	 */
	public WorkerFactory(ISubParserManager subParserManager) {
		this.subParserManager = subParserManager;
	}
	
	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#createWorker()
	 */
	public ParserWorker createWorker() throws Exception {
	    ParserWorker parserWorker = new ParserWorker(this.subParserManager);
	    parserWorker.setPriority(2);
		return parserWorker;
	}

	/**
	 * {@inheritDoc}
	 * @see IWorkerFactory#initWorker(IWorker)
	 */		
	public void initWorker(ParserWorker worker) {
		// nothing todo here
	}
}
