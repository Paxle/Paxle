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
package org.paxle.parser.sitemap.impl;

import java.net.URI;
import java.util.Date;

import org.paxle.parser.sitemap.api.Url;


public class UrlImpl implements Url {
	private URI location;
	private Date lastMod;
	private Float prio;
	private ChangeFrequency freq;
	
	public ChangeFrequency getChangeFreq() {
		return this.freq;
	}
	
	public void setChangeFreq(ChangeFrequency freq) {
		this.freq = freq;
	}

	public Date getLastMod() {
		return this.lastMod;
	}

	public void setLastMod(Date lastMod) {
		this.lastMod = lastMod;
	}
	
	public URI getLocation() {
		return this.location;
	}

	public void setLocation(URI location) {
		this.location = location;
	}	
	
	public Float getPriority() {
		return this.prio;
	}
	
	public void setPriority(Float priority) {
		this.prio = priority;
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();		
		buf.append(this.location.toString());
		return buf.toString();
	}
}
