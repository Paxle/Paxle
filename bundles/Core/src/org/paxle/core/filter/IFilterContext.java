package org.paxle.core.filter;

import java.util.Properties;

import org.paxle.core.io.temp.ITempFileManager;

public interface IFilterContext {	
	public int getFilterPosition();
	public String getTargetID();
	public IFilter getFilter();
	public Properties getFilterProperties();
	
	// TODO: 
	public ITempFileManager getTempFileManager();
}
