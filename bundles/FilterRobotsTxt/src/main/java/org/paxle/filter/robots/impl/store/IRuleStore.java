package org.paxle.filter.robots.impl.store;

import java.io.IOException;

import org.paxle.filter.robots.impl.rules.RobotsTxt;

public interface IRuleStore {
	public RobotsTxt read(String hostPort) throws IOException;
	public void write(RobotsTxt robotsTxt) throws IOException;
	public void close() throws IOException;
}
