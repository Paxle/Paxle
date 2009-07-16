package org.paxle.gui.remote.actions;

import java.util.List;

import org.paxle.gui.remote.Peer;
import org.paxle.gui.remote.PeerService;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public class ListPeerAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	
	private final PeerService service;
	private List<Peer> peers;
	
	public ListPeerAction(PeerService service) {
		this.service = service;
	}
	
	@Override
	public String execute() throws Exception {
		this.peers = this.service.getPeers();
		return Action.SUCCESS;
	}

	public List<Peer> getPeers() {
		return this.peers;
	}
}
