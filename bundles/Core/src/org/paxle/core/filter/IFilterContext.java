package org.paxle.core.filter;

import java.util.Properties;

import org.paxle.core.queue.ICommand;

public interface IFilterContext {	
	public int getFilterPosition();
	public String getTargetID();
	public IFilter getFilter();
	public Properties getFilterProperties();
}
