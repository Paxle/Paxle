
package org.paxle.filter.blacklist.impl;

public class FilterResult {
	
	public final static int LOCATION_OKAY = 0;
	public final static int LOCATION_REJECTED = 1;
	
	public static final FilterResult LOCATION_OKAY_RESULT = new FilterResult(LOCATION_OKAY, null);
	
	private int status;
	private String rejectPattern = null;
	
	public FilterResult(int resultStatus, String pattern) {
		setStatus(resultStatus);
		setRejectPattern(pattern);
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getStatus() {
		return status;
	}
	
	public boolean hasStatus(int status) {
		return this.status == status;
	}
	
	public void setRejectPattern(String rejectPattern) {
		this.rejectPattern = rejectPattern;
	}
	
	public String getRejectPattern() {
		return rejectPattern;
	}
}
