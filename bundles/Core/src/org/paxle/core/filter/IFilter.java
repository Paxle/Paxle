package org.paxle.core.filter;

import org.paxle.core.queue.ICommand;

public interface IFilter<C extends ICommand> {
	public static final String PROP_FILTER_TARGET_ID = "FilterTarget.id";
	public void filter(C command);
}
