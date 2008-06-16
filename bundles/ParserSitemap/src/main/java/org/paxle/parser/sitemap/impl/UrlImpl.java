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
