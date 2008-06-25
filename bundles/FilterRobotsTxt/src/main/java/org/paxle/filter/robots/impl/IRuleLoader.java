package org.paxle.filter.robots.impl;

import java.io.IOException;

import org.paxle.filter.robots.impl.rules.RobotsTxt;

public interface IRuleLoader {
	public RobotsTxt read(String hostPort) throws IOException;
	public void write(RobotsTxt robotsTxt) throws IOException;
}
