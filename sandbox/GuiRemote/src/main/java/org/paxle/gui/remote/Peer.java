package org.paxle.gui.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Peer {

	private String name;
	private String address;
	private HashMap<String, List<PeerStatus>> status = new HashMap<String, List<PeerStatus>>();
	
 	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	
	public List<PeerStatus> getPeerStatus(String type) {
		return this.status.get(type);
	}
	
	public void setPeerStatus(String type, long timeStamp, Number value) {
		List<PeerStatus> stat = this.getPeerStatus(type);
		if (stat == null) {
			stat = new ArrayList<PeerStatus>();
			this.status.put(type, stat);
		}
		stat.add(new PeerStatus(timeStamp, value));
	}
}
