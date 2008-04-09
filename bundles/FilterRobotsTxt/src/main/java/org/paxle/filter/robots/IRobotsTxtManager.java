package org.paxle.filter.robots;

import java.net.URI;
import java.util.Collection;
import java.util.List;

public interface IRobotsTxtManager {
	public boolean isDisallowed(String location);
	public boolean isDisallowed(URI location);
	public List<URI> isDisallowed(Collection<URI> urlList);
	
	public Collection<URI> getSitemaps(String location);
	public Collection<URI> getSitemaps(URI location);
}
