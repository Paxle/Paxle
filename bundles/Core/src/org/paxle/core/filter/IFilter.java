package org.paxle.core.filter;

import org.paxle.core.queue.ICommand;

public interface IFilter<C extends ICommand> {
	public void filter(C command);
}
