package org.paxle.tools.dns.impl;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.paxle.tools.dns.IAddressTool;
import org.xbill.DNS.Address;

/**
 * Just a wrapper for {@link org.xbill.DNS.Address}
 */
public class AddressTool implements IAddressTool {
	public InetAddress getByName(String hostName) throws UnknownHostException {
		return Address.getByName(hostName);
	}
}
