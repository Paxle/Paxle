
package org.paxle.data.balancer;

import java.net.URI;

public interface IHostConfigProvider {
	
	public IHostConfig getHostConfig(final URI uri);
}
