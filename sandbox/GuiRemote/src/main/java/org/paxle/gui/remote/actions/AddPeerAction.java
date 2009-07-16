package org.paxle.gui.remote.actions;

import org.paxle.gui.remote.Peer;
import org.paxle.gui.remote.PeerService;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.ModelDriven;

public class AddPeerAction extends ActionSupport implements ModelDriven<Peer> {
	private static final long serialVersionUID = 1L;
	
	private final PeerService service;
	private Peer peer = new Peer();
	
	public AddPeerAction(PeerService service) {
		this.service = service;
	}
	
	@Override
	public String execute() throws Exception {
		if (this.peer != null && this.peer.getName() != null && this.peer.getAddress() != null) {
			this.service.addPeer(peer);
			return Action.SUCCESS;
		}		
		return Action.INPUT;
	}

	public Peer getModel() {
		return peer;
	}
}
