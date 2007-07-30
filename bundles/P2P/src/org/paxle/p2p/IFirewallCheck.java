package org.paxle.p2p;

public interface IFirewallCheck {
	int FIREWALLED=0;
	int NOT_FIREWALLED=1;
	int CHECKING=2;
	int NOT_TESTED=3;
	public void startFirewallCheck(int timeout);
	public int getStatus();
}
