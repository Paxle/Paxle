package org.paxle.core.filter.impl;

import java.util.Properties;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;


public class FilterContext implements Comparable<FilterContext>, IFilterContext {
	private Properties props = null;
	private IFilter filterImpl = null;
	private String targetID = null;
	private int pos = 0;
	
	
	public FilterContext(IFilter filterImpl, String targetID, int filterPos, Properties props) {
		this.filterImpl = filterImpl;
		this.targetID = targetID;
		this.pos = filterPos;
		this.props = props;
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
	
	public int compareTo(FilterContext o) {
		return Integer.valueOf(this.pos).compareTo(Integer.valueOf(o.pos));
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
