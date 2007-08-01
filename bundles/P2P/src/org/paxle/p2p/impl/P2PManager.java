package org.paxle.p2p.impl;

import java.io.File;
import java.io.IOException;

import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;

import org.paxle.p2p.IP2PManager;

import com.axlight.jnushare.gisp.GISPImpl;

public class P2PManager implements IP2PManager {
	private NetworkManager manager = null;
	private PeerGroup group = null;
	private GISPImpl gisp = null;
	
	public void init() {
		// init JXTA
		try {
			manager = new NetworkManager(
					// the network mode
					NetworkManager.ConfigMode.EDGE,
					// the peer name
					"DiscoveryServer",
					new File(new File(".cache"), "DiscoveryServer").toURI());
			manager.startNetwork();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		this.group = manager.getNetPeerGroup();

		// init GISP
		gisp = new GISPImpl();
		gisp.init(group, null, null);
		gisp.startApp(null);

//		gisp.insert("tag1", "this is a string");
//		gisp.query("tag1", new ResultListener(){
//			public void stringResult(String data){
//				System.out.println("Got result: " + data);
//			}
//			public void xmlResult(byte[] data){
//			}
//			public void queryExpired(){
//			}
//		});		
	}
	
	public void stop() {
		// TODO: close pipes
		
		// stop gisp
		gisp.stopApp();
		
		// stop network
		manager.stopNetwork();
	}
	
	public void setMode(ConfigMode mode) {
		// change the config-mode
		try {
			this.manager.setMode(mode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPeerID() {
		return this.manager.getPeerID().toString();
	}
	
	public String getPeerName() {
		return this.group.getPeerName();
	}
		
	public String getGroupID() {
		return this.group.getPeerGroupID().toString();
	}
	
	public String getGroupName() {
		return this.group.getPeerGroupName();
	}
	
}
