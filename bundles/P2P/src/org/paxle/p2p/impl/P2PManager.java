package org.paxle.p2p.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.credential.Credential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocument;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.StructuredTextDocument;
import net.jxta.document.XMLDocument;
import net.jxta.id.IDFactory;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.platform.NetworkManager;
import net.jxta.platform.NetworkManager.ConfigMode;
import net.jxta.protocol.DiscoveryResponseMsg;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.p2p.IP2PManager;

import com.axlight.jnushare.gisp.GISPImpl;

public class P2PManager implements IP2PManager, DiscoveryListener {
	/**
	 * The default paxle jxta group name
	 */
	private static final String DEFAULT_PEER_GROUP = "paxle";
	
	/**
	 * Jxta Network-Manager
	 */
	private NetworkManager manager = null;
	
	/**
	 * The paxle peer group
	 */
	private PeerGroup paxleGroup = null;
	
	/**
	 * DHT lib
	 */
	private GISPImpl gisp = null;
	
	/**
	 * Logger class
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Connects to the jxta network
	 */
	public void init(File configPath) {
		// init JXTA
		try {
			this.logger.info("Starting JXTA ....");
			manager = new NetworkManager(
					// the network mode
					NetworkManager.ConfigMode.EDGE,
					// the peer name
					P2PTools.getComputerName(),
					configPath.toURI());			
			manager.startNetwork();
			//boolean connectedToRendezVous = manager.waitForRendezvousConnection(60000);
			//this.logger.info("Peer " + (connectedToRendezVous?"":"not") + "connected to rendezvous");
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		// create our Paxle peer group
		try {
			this.paxleGroup = this.createPaxleGroup();
		} catch (Exception e) {
			this.logger.error("Paxle group creation failed",e);
		} 
		
		// join the group
		this.joinPaxleGroup();

		// init GISP
		gisp = new GISPImpl();
		gisp.init(paxleGroup, null, null);
		gisp.startApp(null);
		
		// Jxta Shell for debugging
//		Shell shell = new Shell();
//		shell.init(this.paxleGroup,null,null);
//		shell.startApp(null);

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
	
	public String[] getPeerList() {
		List<String> peers = new ArrayList<String>();
		
		try {			
			// obtain the the discovery service
			DiscoveryService discoSvc = this.paxleGroup.getDiscoveryService();
			discoSvc.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, 1000);
			
			Enumeration<Advertisement> advs = discoSvc.getLocalAdvertisements(DiscoveryService.PEER, null, null);
			while (advs.hasMoreElements()) {
				Advertisement adv=advs.nextElement();
				if (adv instanceof PeerAdvertisement) {
					PeerAdvertisement peerAdv = (PeerAdvertisement) adv;
					peers.add(peerAdv.getName());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return peers.toArray(new String[peers.size()]);
	}
	
	/**
	 * Creates a unique jxta {@link PeerGroupID group-id} for paxle
	 * @return the unique group id
	 */
	private PeerGroupID createPaxleGroupID() {
		return P2PTools.createPeerGroupID(DEFAULT_PEER_GROUP);
	}
		
	/**
	 * Creates a new jxta {@link PeerGroup peer-group} and publishes it.
	 * @return the newly created jxta peer-group
	 * @throws Exception
	 */
	private PeerGroup createPaxleGroup() throws Exception {
        PeerGroupAdvertisement adv;

        System.out.println("Creating a new group advertisement");

        PeerGroup netGroup = null;
        PeerGroup paxleGroup = null;
        ModuleImplAdvertisement implAdv = null;
        try {
            // create, and start the default jxta NetPeerGroup
        	netGroup = manager.getNetPeerGroup();
        	
            // create a new all purpose peergroup.
            implAdv = netGroup.getAllPurposePeerGroupImplAdvertisement();
            System.out.println("Spec-ID: " + implAdv.getModuleSpecID());

            // create the paxle group
            paxleGroup = netGroup.newGroup(
            		// the jxta group ID to use
            		this.createPaxleGroupID(),
            		// the advertisment
            		implAdv,
            		// the group name
            		DEFAULT_PEER_GROUP,
            		// description of the group
            		"paxle p2p group"
            );

            // print the name of the group and the peer group ID
            adv = paxleGroup.getPeerGroupAdvertisement();
            PeerGroupID GID = adv.getPeerGroupID();
            System.out.println("  Group = " +adv.getName() +
                               "\n  Group ID = " + GID.toString());

        } catch (Exception eee) {
            System.out.println("Group creation failed with " + eee.toString());
            throw eee;
        }

        try {
            // obtain the the discovery service from the netgroup
        	DiscoveryService netGroupDiscovery = netGroup.getDiscoveryService();
        	
            // publish this advertisement
        	netGroupDiscovery.publish(adv);
        	netGroupDiscovery.remotePublish(implAdv); // do we need this?
            netGroupDiscovery.remotePublish(adv);
            System.out.println("Group published successfully.");
        } catch (Exception e) {
            System.out.println("Error publishing group advertisement");
            e.printStackTrace();
            throw e;
        }
        
    	paxleGroup.getDiscoveryService().addDiscoveryListener(this);        
        return paxleGroup;
	}
	
	/**
	 * Joins the peer to the paxle peer group
	 */
	private void joinPaxleGroup()  {
		System.out.println("Joining peer group...");
        
		StructuredDocument creds = null;
        
		try {
			// Generate the credentials for the Peer Group
			AuthenticationCredential authCred = new AuthenticationCredential( this.paxleGroup, null, creds );
            
			// Get the MembershipService from the peer group
			MembershipService membership = this.paxleGroup.getMembershipService();
            
			// Get the Authenticator from the Authentication creds
			Authenticator auth = membership.apply( authCred );
            
			// Check if everything is okay to join the group
			if (auth.isReadyForJoin()) {
				Credential myCred = membership.join(auth);
				this.logger.info("Peer successfully joined to group " + this.paxleGroup.getPeerGroupName());
                
				// display the credential as a plain text document.
				StructuredTextDocument doc = (StructuredTextDocument) myCred.getDocument(new MimeMediaType("text/plain"));
                
				StringWriter out = new StringWriter();
				doc.sendToWriter(out);
				this.logger.info("Credential: " + out.toString());
				out.close();
			} else {
				this.logger.error("Unable to join paxle peer-group. Authenticator was not ready for join.");
			}
		} catch (Exception e) {
			this.logger.error("Unable to join paxle peer-group. Failure in authentication.",e);
		}
	}
	
	
	public void stop() {
		// TODO: close pipes
		
		// stop gisp
		gisp.stopApp();
		
		// stop group
		this.paxleGroup.stopApp();
		manager.getNetPeerGroup().stopApp();		
		
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
		return this.paxleGroup.getPeerID().getUniqueValue().toString();
	}
	
	public String getPeerName() {
		return this.paxleGroup.getPeerName();
	}
		
	public String getGroupID() {
		return this.paxleGroup.getPeerGroupID().getUniqueValue().toString();
	}
	
	public String getGroupName() {
		return this.paxleGroup.getPeerGroupName();
	}

	/**
	 * @see DiscoveryListener
	 */
	public void discoveryEvent(DiscoveryEvent event) {
	      DiscoveryResponseMsg response = event.getResponse();
	      java.util.Enumeration responses = response.getResponses();
	      while (responses.hasMoreElements()) {
	         String responseElement = (String) responses.nextElement();
	         if (null != responseElement) {
	            try {
	               ByteArrayInputStream stream = new ByteArrayInputStream(responseElement.getBytes());
	               XMLDocument asDoc = (XMLDocument) StructuredDocumentFactory.
	                  newStructuredDocument(MimeMediaType.XMLUTF8, stream);
	               Advertisement adv = AdvertisementFactory.newAdvertisement(asDoc);
	               this.logger.info( "Advertisement received:  " + adv.getAdvType());
	            } catch (IOException e) {
	            	this.logger.warn("Error in discoveryEvent: " + e);
	            }
	         } else {
	        	 this.logger.warn("Response advertisement is null!");
	         }
	      }
	}
}
