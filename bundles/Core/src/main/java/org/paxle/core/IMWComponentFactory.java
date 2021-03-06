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

package org.paxle.core;

import java.io.IOException;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.paxle.core.doc.ICommand;
import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

/**
 * Factory class to create a {@link IMWComponent master-worker-component}.
 * An instance of this interface is deployed as osgi service and can be used
 * by other bundles to create a new {@link IMWComponent master-worker-component}.
 */
public interface IMWComponentFactory {
	/**
	 * @param workerFactory
	 * @param queueBufferSize
	 * @return a new {@link IMWComponent master-worker-component}
	 */
	public <Data,W extends IWorker<Data>> IMWComponent<Data> createComponent(
			IWorkerFactory<W> workerFactory,
			int queueBufferSize,
			Class<Data> clazz);
	
	public <Data extends ICommand,W extends IWorker<Data>> IMWComponent<Data> createCommandComponent(
			IWorkerFactory<W> workerFactory,
			int queueBufferSize,
			Class<Data> clazz);
	
	public void registerComponentServices(IMWComponent<?> component, BundleContext bc) throws IOException;
	
	public void unregisterComponentServices(String componentID, IMWComponent<?> component, BundleContext bc);

	/**
	 * This function registers a previously created {@link IMWComponent} as multiple services to the OSGi framework.
	 * This component and some of it's fields are registered as:
	 * <ul>
	 * 	<li>{@link IMWComponent}</li>
	 *  <li>{@link org.paxle.core.data.IDataSink}</li>
	 *  <li>{@link org.paxle.core.data.IDataSource}</li>
	 *  <li>{@link org.paxle.core.filter.IFilterQueue}</li>
	 *  <li>{@link org.osgi.service.cm.ManagedService}</li>
	 * </ul> 
	 * @param componentID a systemwidth unique component ID. This id is also used by the configuration-management as {@link Constants#SERVICE_PID}
	 * @param componentName the name of this component. This value is used by the configuration-management and displayed to the user
	 * @param componentDescription a textual description. This value is used by the configuration-management and displayed to the user
	 * @param component the previously created {@link IMWComponent}
	 * @param bc 
	 * @throws IOException
	 */
	public void registerComponentServices(String componentID, String componentName, String componentDescription, IMWComponent<?> component, BundleContext bc) throws IOException;
}
