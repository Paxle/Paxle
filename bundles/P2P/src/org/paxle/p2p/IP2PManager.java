package org.paxle.p2p;

import java.util.List;

public interface IP2PManager {
	public String getPeerID();
	public String getPeerName();
	
	public String getGroupID();
	public String getGroupName();
	public String[] getPeerList();
}
