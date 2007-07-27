package org.paxle.core;

import org.paxle.core.threading.IWorker;
import org.paxle.core.threading.IWorkerFactory;

/**
 * Factory class to create a {@link IMWComponent master-worker-component}.
 * An instance of this interface is deployed as osgi service and can be used
 * by other bundles to create a new {@link IMWComponent master-worker-component}.
 */
public interface IMWComponentManager {
	/**
	 * @param workerFactory
	 * @param queueBufferSize
	 * @return a new {@link IMWComponent master-worker-component}
	 */
	public <Data,W extends IWorker<Data>> IMWComponent<Data> createComponent(
			IWorkerFactory<W> workerFactory,
			int queueBufferSize,
			Class<Data> clazz);
}
