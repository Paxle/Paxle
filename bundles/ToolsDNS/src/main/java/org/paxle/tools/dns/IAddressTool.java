package org.paxle.tools.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface IAddressTool {
	public InetAddress getByName(String hostName) throws UnknownHostException;
}
