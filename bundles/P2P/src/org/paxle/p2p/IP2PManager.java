package org.paxle.p2p;

import java.util.List;

import net.jxta.peer.PeerID;
import net.jxta.protocol.PeerAdvertisement;


public interface IP2PManager {
	/**
	 * @return the JXTA {@link net.jxta.peer.PeerID} of this peer as {@link String}
	 */
	public String getPeerID();
	
	/**
	 * @return the name of this peer as reported to the {@link #getGroupName() peer-group}
	 */
	public String getPeerName();
	
	/**
	 * @return the ID of the paxle peer-group
	 */
	public String getGroupID();
	
	/**
	 * @return the name of the paxle peer-group
	 */
	public String getGroupName();
	
	/**
	 * @return the names of all known paxle peer
	 */
	public List<String> getPeerList();
	
	/**
	 * @return the {@link PeerAdvertisement peer-advertisements} of all peers that are 
	 * 		  currently known to this peer.
	 */
	public List<PeerAdvertisement> getPeerAdvertisements();
}
