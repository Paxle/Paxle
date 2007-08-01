package org.paxle.p2p.impl;

import java.io.File;
import java.io.IOException;

import net.jxta.peergroup.PeerGroup;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;

import com.axlight.jnushare.gisp.GISPImpl;
import com.axlight.jnushare.gisp.ResultListener;

public class P2PManager {
	private NetworkManager manager = null;	
	
	public void init() {
		try {
			manager = new NetworkManager(NetworkManager.ConfigMode.EDGE, "DiscoveryServer",
					new File(new File(".cache"), "DiscoveryServer").toURI());
			manager.startNetwork();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		PeerGroup group = manager.getNetPeerGroup();	  


		GISPImpl gisp = new GISPImpl();
		gisp.init(group, null, null);
		gisp.startApp(null);


		gisp.insert("tag1", "this is a string");

		gisp.query("tag1", new ResultListener(){
			public void stringResult(String data){
				System.out.println("Got result: " + data);
			}
			public void xmlResult(byte[] data){
			}
			public void queryExpired(){
			}
		});		
	}
	
	public void stop() {
		// TODO: shutdown P2P network
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
}
