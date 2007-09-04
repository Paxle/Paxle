package org.paxle.core.filter.impl;

import org.paxle.core.filter.IFilter;

public class FilterMetaData implements Comparable<FilterMetaData> {
	private IFilter filterImpl = null;
	private String targetID = null;
	private int pos = 0;
	
	
	public FilterMetaData(IFilter filterImpl, String targetID, int filterPos) {
		this.filterImpl = filterImpl;
		this.targetID = targetID;
		this.pos = filterPos;
	}
	
	public IFilter getFilterImpl() {
		return this.filterImpl;
	}
	
	public String getTargetID() {
		return this.targetID;
	}
	
	public int getFilterPosition(){
		return this.pos;
	}

	public int compareTo(FilterMetaData o) {
		return Integer.valueOf(this.pos).compareTo(Integer.valueOf(o.pos));
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(this.filterImpl.getClass().getName())
			   .append(": ")
			   .append("target=")
			   .append(this.targetID)
			   .append("[").append(this.pos).append("]");
		
		return builder.toString();
	}
}
