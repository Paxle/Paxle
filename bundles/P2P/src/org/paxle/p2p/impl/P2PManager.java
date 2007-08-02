package org.paxle.p2p.impl;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import net.jxta.discovery.DiscoveryService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupFactory;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.paxle.p2p.IP2PManager;

import com.axlight.jnushare.gisp.GISPImpl;

public class P2PManager implements IP2PManager {
	private static final String DEFAULT_PEER_GROUP = "paxle";
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
		
		try {
			this.group = this.createPeerGroup();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //manager.getNetPeerGroup();

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
	
	private PeerGroupID createGroupID(String groupName) {
		return P2PTools.createPeerGroupID(groupName);
	}
	
	private PeerGroup createPeerGroup() throws Exception {
        PeerGroupAdvertisement adv;

        System.out.println("Creating a new group advertisement");

        PeerGroup netGroup = null;
        PeerGroup pg = null;
        try {
            // create, and start the default jxta NetPeerGroup
        	netGroup = manager.getNetPeerGroup();
        	
            // create a new all purpose peergroup.
            ModuleImplAdvertisement implAdv =
                netGroup.getAllPurposePeerGroupImplAdvertisement();

            pg = netGroup.newGroup(this.createGroupID(DEFAULT_PEER_GROUP), // Assign the group ID
                                            implAdv,              // The implem. adv
                                            DEFAULT_PEER_GROUP,            // The name
                                            "paxle p2p group"); // Helpful descr.

            // print the name of the group and the peer group ID
            adv = pg.getPeerGroupAdvertisement();
            PeerGroupID GID = adv.getPeerGroupID();
            System.out.println("  Group = " +adv.getName() +
                               "\n  Group ID = " + GID.toString());

        } catch (Exception eee) {
            System.out.println("Group creation failed with " + eee.toString());
            throw eee;
        }

        try {
            // obtain the the discovery service
        	DiscoveryService discoSvc = netGroup.getDiscoveryService();
        	
            // publish this advertisement
            //(send out to other peers and rendezvous peer)
            discoSvc.remotePublish(adv);
            System.out.println("Group published successfully.");
        } catch (Exception e) {
            System.out.println("Error publishing group advertisement");
            e.printStackTrace();
            throw e;
        }
        
        return pg;
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
