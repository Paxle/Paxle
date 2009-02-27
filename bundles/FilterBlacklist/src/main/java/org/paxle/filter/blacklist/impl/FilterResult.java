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
package org.paxle.filter.blacklist.impl;

import org.paxle.filter.blacklist.IFilterResult;

public class FilterResult implements IFilterResult {
	
	public final static int LOCATION_OKAY = 0;
	public final static int LOCATION_REJECTED = 1;
	
	public static final IFilterResult LOCATION_OKAY_RESULT = new FilterResult(LOCATION_OKAY, null);
	
	private int status;
	private String rejectPattern = null;
	
	public FilterResult(int resultStatus, String pattern) {
		setStatus(resultStatus);
		setRejectPattern(pattern);
	}
	
	public void setStatus(int status) {
		this.status = status;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IFilterResult#getStatus()
	 */
	public int getStatus() {
		return status;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IFilterResult#hasStatus(int)
	 */
	public boolean hasStatus(int status) {
		return this.status == status;
	}
	
	public void setRejectPattern(String rejectPattern) {
		this.rejectPattern = rejectPattern;
	}
	
	/* (non-Javadoc)
	 * @see org.paxle.filter.blacklist.impl.IFilterResult#getRejectPattern()
	 */
	public String getRejectPattern() {
		return rejectPattern;
	}
}
