package org.paxle.core.filter;

import org.paxle.core.queue.ICommand;

public interface IFilter<C extends ICommand> {
	/**
	 * Property to specify the target of a {@link IFilter filter}, e.g.
	 * <pre>org.paxle.parser.out; pos=10</pre>
	 */
	public static final String PROP_FILTER_TARGET = "FilterTarget";
	
	/**
	 * Sub-property of {@link #PROP_FILTER_TARGET}. This can be used
	 * to specify the position of a {@link IFilter filter} within the 
	 * filters-list of a {@link IFilterQueue}, 
	 * 
	 * e.g.
	 * <pre>
	 * Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
	 * filterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{"org.paxle.parser.out; pos=10"});
	 * bc.registerService(IFilter.class.getName(), new MyFilter(), filterProps);	
	 * </pre>
	 * 
	 * If this sub-parameter is not defined the default position <code>0</code> is used.
	 */
	public static final String PROP_FILTER_TARGET_POSITION = "pos";
	
	/**
	 * Function to process a given {@link ICommand} by a filter.
	 * @param command the {@link ICommand} to process
	 * @param filterContext an object containing context-specific parameters that should
	 * be passed to the filter. 
	 */
	public void filter(C command, IFilterContext filterContext);
}
