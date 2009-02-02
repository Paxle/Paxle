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
package org.paxle.core.filter;

public interface IFilter<C> {
	/**
	 * Property to specify the target of a {@link IFilter filter}, e.g.:
	 * <pre>org.paxle.parser.out; pos=10</pre>
	 * 
	 * You can also pass additional target- and position- related properties
	 * to the filter, e.g.:
	 * <pre>org.paxle.parser.out; pos=10; key=value</pre>
	 * 
	 * These additional properties are then accessible by the filter via
	 * {@link IFilterContext#getFilterProperties()}.
	 * <p/>
	 * The value of this property must be an array of strings.
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
	 * filterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{String.format("org.paxle.parser.out; %s=%d", IFilter.PROP_FILTER_TARGET_POSITION,10)});
	 * bc.registerService(IFilter.class.getName(), new MyFilter(), filterProps);	
	 * </pre>
	 * 
	 * If this sub-parameter is not defined the default position <code>0</code> is used.
	 */
	public static final String PROP_FILTER_TARGET_POSITION = "pos";
	
	/**
	 * Sub-property of {@link #PROP_FILTER_TARGET}. This can be used
	 * to specify if the {@link IFilter filter} should be inactive by default
	 * for the given {@link #PROP_FILTER_TARGET_POSITION position} within the 
	 * filters-list of a {@link IFilterQueue}.
	 * 
	 * e.g.
	 * <pre>
	 * Hashtable<String, String[]> filterProps = new Hashtable<String, String[]>();
	 * filterProps.put(IFilter.PROP_FILTER_TARGET, new String[]{String.format("org.paxle.parser.out; %s=%b", IFilter.PROP_FILTER_TARGET_DISABLED,Boolean.TRUE)});
	 * bc.registerService(IFilter.class.getName(), new MyFilter(), filterProps);	
	 * </pre>
	 * 
	 * If this sub-parameter is not defined the default value is <code>disabled=false</code>
	 */
	public static final String PROP_FILTER_TARGET_DISABLED = "disabled";
	
	/**
	 * Function to process a given {@link ICommand} by a filter.
	 * 
	 * @param command the object to process. Note that a filter receives all objects the queue 
	 * transports, independently of their result status.
	 * @param filterContext an object containing context-specific parameters that should
	 * be passed to the filter. 
	 */
	public void filter(C command, IFilterContext filterContext);
}
