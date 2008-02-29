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
