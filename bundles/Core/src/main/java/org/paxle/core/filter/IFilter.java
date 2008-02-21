package org.paxle.core.filter;

import org.paxle.core.queue.ICommand;

public interface IFilter<C extends ICommand> {
	/**
	 * Property to specify the target of a {@link IFilter filter}, e.g.
	 * <pre>org.paxle.parser.out; pos=10</pre>
	 */
	public static final String PROP_FILTER_TARGET = "FilterTarget";
	
	public void filter(C command, IFilterContext filterContext);
}
