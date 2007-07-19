package org.paxle.core.threading;


public interface IWorkerFactory<E extends IWorker> {
	E makeObject() throws Exception;
}
