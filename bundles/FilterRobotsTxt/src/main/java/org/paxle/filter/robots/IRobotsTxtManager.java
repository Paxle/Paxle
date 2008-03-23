package org.paxle.filter.robots;

import java.net.URI;

public interface IRobotsTxtManager {
	public boolean isDisallowed(String location);
	public boolean isDisallowed(URI location);
}
