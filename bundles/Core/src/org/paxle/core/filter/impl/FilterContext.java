package org.paxle.core.filter.impl;

import java.util.Properties;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.io.temp.ITempFileManager;


public class FilterContext implements Comparable<FilterContext>, IFilterContext {
	private ITempFileManager tempFileManager = null;
	
	private Properties props = null;
	private IFilter filterImpl = null;
	private String targetID = null;
	private int pos = 0;
	
	
	public FilterContext(IFilter filterImpl, String targetID, int filterPos, Properties props) {
		if (filterImpl == null) throw new NullPointerException("Filter class is null");
		if (targetID == null || targetID.length() == 0) throw new IllegalArgumentException("Filter targetID is not set");
		
		this.filterImpl = filterImpl;
		this.targetID = targetID;
		this.pos = filterPos;
		this.props = (props == null) ? new Properties() : props;
	}
	
	public IFilter getFilter() {
		return this.filterImpl;
	}
	
	public String getTargetID() {
		return this.targetID;
	}
	
	public int getFilterPosition(){
		return this.pos;
	}

	public Properties getFilterProperties() {
		return (this.props==null)?new Properties():this.props;
	}
	
	public void setTempFileManager(ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
	}
	
	public ITempFileManager getTempFileManager() {
		return this.tempFileManager;
	}
	
	public int compareTo(FilterContext o) {
		if (this == o) return 0;
		
		// order based on position 
		int comp = Integer.valueOf(this.pos).compareTo(Integer.valueOf(o.pos));
		if (comp != 0) return comp;
		
		// order based on filter-impl class-name
		comp = this.filterImpl.getClass().getName().compareTo(o.filterImpl.getClass().getName());
		if (comp != 0) return comp;
		
		// filter based on properties
		comp = Integer.valueOf(this.props.size()).compareTo(o.props.size());
		if (comp != 0) return comp;
		
		// TODO: is this enough or should we even compare the property values?
		
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(this.filterImpl.getClass().getName())
			   .append(": ")
			   .append("target=")
			   .append(this.targetID)
			   .append("[").append(this.pos).append("] ")
			   .append(this.props.toString());
		
		return builder.toString();
	}
}
