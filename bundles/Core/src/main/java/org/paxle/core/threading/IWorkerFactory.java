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

package org.paxle.core.threading;


public interface IWorkerFactory<E extends IWorker<?>> {
	/**
	 * Function to create a new{@link IWorker worker-instance}
	 * @return a newly created worker
	 * @throws Exception
	 */
	E createWorker() throws Exception;
	
	/**
	 * Function to init a worker befor it starts to process a new Command.
	 * This function is called multiple times during the life-cycle of a
	 * worker instance
	 * @param worker
	 */
	public void initWorker(E worker);
}
