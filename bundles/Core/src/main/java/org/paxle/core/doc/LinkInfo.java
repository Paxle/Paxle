/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.doc;

import java.io.Serializable;
import java.util.HashMap;

import org.paxle.core.filter.IFilter;


public class LinkInfo {
	/**
	 * @see Status
	 */
	public static final String STATUS = "status";

	/**
	 * A textual description of the status. 
	 * This should be set if the status is not set to {@link Status#OK}.
	 * 
	 * @see #STATUS_CODE
	 */
	public static final String STATUS_TEXT = "statusTxt";

	/**
	 * A status-code which should be systemwide unique.
	 * This should be set if the status is not set to {@link Status#OK}.
	 * 
	 * The idea is to have a hierarchical code scheme, which allows
	 * to identify the source of the status, so you can e.g. index links with a special statuscode again later.
	 * 
	 * Examples for statuscodes:
	 * <pre>org.paxle.FilterRobotsTxt/Disallow</pre>
	 * or
	 * <pre>org.paxle.FilterBlacklist/Blocked/myBlacklistName</pre>
	 * 
	 * @see #STATUS_TEXT
	 */
	public static final String STATUS_CODE = "statusCode";

	/**
	 * The link title
	 */
	public static final String TITLE = "title";

	/**
	 * The textual description of the origin of this link, e.g. extracted from document or generated by a filter.<br/>
	 * Examples:
	 * "ParserHtml"
	 * "AscendingPathUrlExtractionFilter"
	 */
	public static final String ORIGIN = "origin";

	/**
	 * The link description
	 */
	public static final String DESCRIPTION = "descr";

	public static enum Status {
		/** Link is allowed to be processed */
		OK,

		/** The link was filtered by one of the {@link IFilter}s */
		FILTERED
	}	

	private HashMap<String, Serializable> props = null;

	public LinkInfo() {
		this(null, null);
	}

	public LinkInfo(String title) {
		this(title, null);
	}

	public LinkInfo(String title, String linkOrigin) {
		this(title, null, linkOrigin);
	}

	public LinkInfo(String title, Status status, String linkOrigin) {
		this(title, status, null, linkOrigin);
	}

	public LinkInfo(String title, Status status, String statusText, String linkOrigin) {
		this(title, status, statusText, null, linkOrigin);
	}

	public LinkInfo(String title, Status status, String statusText, String statusCode, String linkOrigin) {
		this.props = new HashMap<String, Serializable>(8);

		// adding status
		this.setStatus(status, statusText, statusCode);
		// adding title
		this.setTitle(title);
		// adding origin
		this.setLinkOrigin(linkOrigin);
	}

	public String getTitle() {
		return (String) this.props.get(TITLE);
	}

	public void setTitle(String title) {
		if (title != null && title.length() > 0) {
			this.props.put(TITLE, title);			
		} else {
			this.props.remove(TITLE);
		}
	}

	public String getDescription() {
		return (String) this.props.get(DESCRIPTION);
	}

	public Status getStatus() {
		String status = (String)this.props.get(STATUS);
		if (status == null) return Status.OK;
		return Status.valueOf(status);
	}

	public boolean hasStatus(Status status) {
		Status current = this.getStatus();
		return current.equals(status);
	}

	public String getStatusText() {
		return (String) this.props.get(STATUS_TEXT);
	}

	public String getStatusCode() {
		return (String) this.props.get(STATUS_CODE);
	}

	public void setStatus(Status status) {
		this.setStatus(status,null);
	}

	public void setStatus(Status status, String statusText) {
		this.setStatus(status, statusText, null);
	}

	public void setStatus(Status status, String statusText, String statusCode) {
		if (status == null) status = Status.OK;
		this.props.put(STATUS, status.name());

		if (statusText != null && statusText.length() > 0) {
			this.props.put(STATUS_TEXT, statusText);
		} else {
			this.props.remove(STATUS_TEXT);
		}

		if (statusCode != null && statusCode.length() > 0) {
			this.props.put(STATUS_CODE, statusCode);
		} else {
			this.props.remove(STATUS_CODE);
		}
	}

	/**
	 * @see #ORIGIN
	 */
	public String getLinkOrigin() {
		return (String) this.props.get(ORIGIN);
	}

	/**
	 * @see #ORIGIN
	 */
	public void setLinkOrigin(String linkOrigin) {
		if (linkOrigin != null && linkOrigin.length() > 0) {
			this.props.put(ORIGIN, linkOrigin);
		} else {
			this.props.remove(ORIGIN);
		}
	}

	/**
	 * Returns a string representation of the property map of this LinkInfo
	 */
	@Override
	public String toString() {
		return this.props.toString();
	}
}
