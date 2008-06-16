package org.paxle.parser.sitemap.api;

import java.net.URI;
import java.util.Date;

public interface Url {
	public static enum ChangeFrequency {
	    always,
	    hourly,
	    daily,
	    weekly,
	    monthly,
	    yearly,
	    never
	}
	
	public URI getLocation();
	public Date getLastMod();
	public ChangeFrequency getChangeFreq();
	public Float getPriority();
}
