package org.paxle.p2p;

import java.util.List;

import net.jxta.protocol.PeerAdvertisement;


public interface IP2PManager {
	public String getPeerID();
	public String getPeerName();
	
	public String getGroupID();
	public String getGroupName();
	
	public List<String> getPeerList();
	public List<PeerAdvertisement> getPeerAdvertisements();
}
