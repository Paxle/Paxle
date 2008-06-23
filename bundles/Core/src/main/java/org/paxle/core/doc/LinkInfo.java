package org.paxle.core.doc;

import java.io.Serializable;
import java.util.HashMap;

import org.paxle.core.filter.IFilter;


public class LinkInfo {
	public static final String STATUS = "status";
	public static final String STATUS_TEXT = "statusText";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	
	public static enum Status {
		/** Link is allowed to be processed */
		OK,
		
		/** The link was filtered by one of the {@link IFilter}s */
		FILTERED
	}	
	
	private HashMap<String, Serializable> props = null;
	
	public LinkInfo() {
		this(null);
	}
	
	public LinkInfo(String title) {
		this(title, Status.OK);
	}
	
	public LinkInfo(String title, Status status) {
		this(title, status, null);
	}
	
	public LinkInfo(String title, Status status, String statusText) {
		this.props = new HashMap<String, Serializable>();
		
		// adding status
		this.setStatus(status);
		
		// adding title
		if (title != null && title.length() > 0) {
			this.props.put(TITLE, title);			
		}
	}
	
	public String getTitle() {
		return (String) this.props.get(TITLE);
	}
	
	public String getDescription() {
		return (String) this.props.get(DESCRIPTION);
	}
	
	public Status getStatus() {
		String status = (String)this.props.get(STATUS);
		if (status == null) return Status.OK;
		return (Status) Status.valueOf(status);
	}
	
	public boolean hasStatus(Status status) {
		Status current = this.getStatus();
		return current.equals(status);
	}
	
	public String getStatusText() {
		return (String) this.props.get(STATUS_TEXT);
	}
	
	public void setStatus(Status status) {
		this.setStatus(status,null);
	}
	
	public void setStatus(Status status, String statusText) {
		if (status == null) status = Status.OK;
		this.props.put(STATUS, status.name());
		
		if (statusText != null && statusText.length() > 0) {
			this.props.put(STATUS_TEXT, statusText);
		}
	}
	
	@Override
	public String toString() {
		return this.props.toString();
	}
	
}