package org.paxle.p2p.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Semaphore;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLElement;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.membership.Authenticator;
import net.jxta.membership.MembershipService;
import net.jxta.peergroup.NetPeerGroupFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.InputPipe;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.ModuleSpecID;
import net.jxta.platform.NetworkConfigurator;
import net.jxta.platform.NetworkManager.ConfigMode;
import net.jxta.protocol.ConfigParams;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.PeerAdvertisement;
import net.jxta.protocol.PeerGroupAdvertisement;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.rendezvous.RendezVousService;
import net.jxta.rendezvous.RendezvousEvent;
import net.jxta.rendezvous.RendezvousListener;
import net.jxta.util.PipeEventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.p2p.IP2PManager;

public class P2PManager extends Thread implements IP2PManager, RendezvousListener, PipeMsgListener, DiscoveryListener {

	private static final long PEER_ADV_EXPIRATION = 1000*60*15;

	/* ==============================================================
	 * JXTA Netpeer group constants
	 * ============================================================== */    
	private static final String NETPG_GID = "urn:jxta:uuid-0345A1D0CDB24759854AC5FA4597B7B502";
	private static final String NETPG_NAME = "Paxle NetPG";
	private static final String NETPG_DESC = "A Private Paxle Net Peer Group";

	/* ==============================================================
	 * JXTA Application group constants
	 * ============================================================== */
	private static final String APPGRP_NAME = "Paxle Peer Group";
	private static final String APPGRP_DESC = "Paxle Peer Group Description goes here";
	private static final String APPGRP_GID =  "urn:jxta:uuid-A26A20420CC24B85AA07C805E83D497C02";
	private static final String APPGRP_SPECID = "urn:jxta:uuid-B7EA1DAECC7740BF85E7A939E3441CF4BCCD1EDF355A4FC1BD47FBA5A8E5842A06";

	private Log logger = LogFactory.getLog(this.getClass());

	/**
	 * The configuration of this peer
	 */
	private NetworkConfigurator configurator;

	private DiscoveryService netPGDiscoveryService;
	private DiscoveryService appPGDiscoveryService;
	private RendezVousService appPGRdvService;
	private RendezVousService netPGRdvService;
	private OutputPipe outputPipe;
	private InputPipe inputPipe;
	private PeerGroup netPeerGroup;
	private PeerGroup appPeerGroup;

	private String jxtaHome;

	private Semaphore rdvLock = new Semaphore(0);

	private String myPeerID;	

	private URI seedingURI = null;

	public P2PManager(File home, URI seedURI) throws IOException {
		if (home == null) throw new NullPointerException("The config directory is null.");

		jxtaHome = home.getCanonicalPath();
		this.seedingURI = seedURI;

		// configure jxta 
		this.configureJXTA();
		try {
			this.startJXTA();
			this.createApplicationPeerGroup();
		} catch(Throwable e) {
			e.printStackTrace();
			System.out.println("Exiting.");
			System.exit(1);
		}				

		this.start();
	}

	@Override
	public void run() {
		try {
			this.waitForRdv();
			this.publishPeerAdv();
			this.doSomething();
		} catch(Throwable e) {
			e.printStackTrace();
			System.out.println("Exiting.");
			System.exit(1);
		}		
	}

	// -------------------------------------

	private void startJXTA() throws Throwable {

		clearCache(new File(jxtaHome,"cm"));

		NetPeerGroupFactory factory=null;
		try {
			factory = new NetPeerGroupFactory(
					// net peer group configuration
					(ConfigParams)configurator.getPlatformConfig(),
					// persistent storage location
					new File(jxtaHome).toURI(),
					// group ID
					IDFactory.fromURI(new URI(NETPG_GID)),
					// group name
					NETPG_NAME,
					// group description
					(XMLElement) StructuredDocumentFactory.newStructuredDocument(MimeMediaType.XMLUTF8,
							"desc", NETPG_NAME)
			);
		}
		catch(URISyntaxException e) {
			e.printStackTrace();
			System.out.println("Exiting...");
			System.exit(1);
		}
		netPeerGroup = factory.getInterface();

		netPGDiscoveryService = netPeerGroup.getDiscoveryService();

		netPGRdvService = netPeerGroup.getRendezVousService();
		netPGRdvService.addListener(this);
	}

	// -------------------------------------

	public void createApplicationPeerGroup() {

		//  create the new application group, and publish its various advertisements
		try {			
			ModuleImplAdvertisement implAdv = netPeerGroup.getAllPurposePeerGroupImplAdvertisement();
			ModuleSpecID modSpecID = (ModuleSpecID )IDFactory.fromURI(new URI(APPGRP_SPECID));
			implAdv.setModuleSpecID(modSpecID);
			PeerGroupID groupID = (PeerGroupID )IDFactory.fromURI(new URI(APPGRP_GID));
			appPeerGroup = netPeerGroup.newGroup(groupID, implAdv, APPGRP_NAME, APPGRP_DESC);
			PeerGroupAdvertisement pgadv = appPeerGroup.getPeerGroupAdvertisement();

			appPGRdvService = appPeerGroup.getRendezVousService();	
			appPGDiscoveryService = appPeerGroup.getDiscoveryService();

			myPeerID = appPeerGroup.getPeerID().toString();

			netPGDiscoveryService.publish(implAdv);
			netPGDiscoveryService.publish(pgadv);
			netPGDiscoveryService.remotePublish(null,implAdv);
			netPGDiscoveryService.remotePublish(null,pgadv);	         	         

			// listen for app group rendezvous events
			appPeerGroup.getRendezVousService().addListener(this);
			
			// join the group
			if (appPeerGroup != null) {
				AuthenticationCredential cred = new AuthenticationCredential(appPeerGroup, null, null);
				MembershipService membershipService = appPeerGroup.getMembershipService();
				Authenticator authenticator = membershipService.apply(cred);
				if (authenticator.isReadyForJoin()) {
					membershipService.join(authenticator);
					System.out.println("Joined group: " + appPeerGroup);
				}
				else {
					System.out.println("Impossible to join the group");
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			System.out.println("Exiting.");
			System.exit(1);
		}

	}

	public void publishPeerAdv() {
		try {
//			boolean found = false;
//			do {
				DiscoveryService ds = appPeerGroup.getDiscoveryService();
				
				// TODO: publish until our advertisement can be found remotely
				ds.publish(appPeerGroup.getPeerAdvertisement());
				ds.remotePublish(appPeerGroup.getPeerAdvertisement());

//				// FIXME: seems not to work properly
//				System.out.println("Search for own peer.");
//				
//				ds.getRemoteAdvertisements(null, DiscoveryService.PEER, "PID", this.getPeerID(), 2000, new DiscoveryListener() {
//
//					public void discoveryEvent(DiscoveryEvent event) {
//						// TODO Auto-generated method stub
//						System.out.println("FOUND");
//					}
//					
//				});
//				
//				Thread.sleep(2000);
//				
//			} while (!found);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -----------------------------------

	public void terminate() {
		// TODO: stop all startet P2P services		   

		// leave the app-peer-group
		try {
			this.appPeerGroup.getMembershipService().resign();
		} catch (PeerGroupException e) {
			this.logger.warn(String.format(
					"Unable to resign membership of group '%s'.",
					this.appPeerGroup.getPeerGroupName()
			));
		}

		// leave membership in the net group
		try {
			netPeerGroup.getMembershipService().resign();
		} catch (PeerGroupException e) {
			this.logger.warn(String.format(
					"Unable to resign membership of group '%s'.",
					this.netPeerGroup.getPeerGroupName()
			));
		}

		// stop the net peer group
		netPeerGroup.stopApp();
	}

	// -----------------------------------

	/**
	 * TODO: we only need to create a new config if the old configuration can not be loaded from file.
	 * e.g.
	 * <code>if (configurator.exists()) { 
	 * 	// load config from file 
	 * }</code>
	 */
	private void configureJXTA() {
		this.configurator = new NetworkConfigurator();
		this.configurator.setHome(new File(this.jxtaHome));

		try {
			if (this.configurator.exists()) {
				this.configurator.load();
			} else {		
				// configure the unique ID of this peer
				// FIXME: don't recreate this ID each time
				this.configurator.setPeerID(IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID));

				// the name of this peer and description
				this.configurator.setName(P2PTools.getComputerName());
				this.configurator.setDescription("I am a P2P Peer.");

				// auth. info (do we realy need this?)
				this.configurator.setPrincipal("ofno");
				this.configurator.setPassword("consequence");

				/*
				 * Turn off mulitcast.
				 * 
				 * "Multicast should be used only if you know what you are doing. 
				 *  Multicast on can make it look like your app is working when it isn't."
				 *  See: http://wiki.java.net/bin/view/Jxta/NetworkBasics
				 */
				this.configurator.setUseMulticast(false);

//				this.configurator.setMode(NetworkConfigurator.EDGE_NODE);

				// only use peers specified in the SeedingURI as relay/rendezvous peer (don't change this!)
				this.configurator.setUseOnlyRelaySeeds(true);
				this.configurator.setUseOnlyRendezvousSeeds(true);

				// edge peers do not need incoming requests
				this.configurator.setTcpIncoming(false);
			}
			
			// setting the rdvz/relay-peers for bootstrapping
			this.configurator.clearRendezvousSeedURIs();
			this.configurator.addRdvSeedingURI(seedingURI);
			this.configurator.clearRelaySeedingURIs();
			this.configurator.addRelaySeedingURI(seedingURI);
			

			// save configuration to file
			this.configurator.save();
			System.out.println("Platform configured and saved");			
			
		} catch(Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	// ---------------------------------

	/**
	 * Events from the rendezvous service. 
	 * @see RendezvousListener#rendezvousEvent(RendezvousEvent)
	 */
	public void rendezvousEvent(RendezvousEvent event) {
		boolean connected = false;
		String eventDescription;
		int eventType = event.getType();
		switch( eventType ) {
		case RendezvousEvent.RDVCONNECT:
			eventDescription = "RDVCONNECT";
			connected=true;
			break;
		case RendezvousEvent.RDVRECONNECT:
			eventDescription = "RDVRECONNECT";
			connected=true;
			break;
		case RendezvousEvent.RDVDISCONNECT:
			eventDescription = "RDVDISCONNECT";
			break;
		case RendezvousEvent.RDVFAILED:
			eventDescription = "RDVFAILED";
			break;
		case RendezvousEvent.CLIENTCONNECT:
			eventDescription = "CLIENTCONNECT";
			break;
		case RendezvousEvent.CLIENTRECONNECT:
			eventDescription = "CLIENTRECONNECT";
			break;
		case RendezvousEvent.CLIENTDISCONNECT:
			eventDescription = "CLIENTDISCONNECT";
			break;
		case RendezvousEvent.CLIENTFAILED:
			eventDescription = "CLIENTFAILED";
			break;
		case RendezvousEvent.BECAMERDV:
			eventDescription = "BECAMERDV";
			connected=true;
			break;
		case RendezvousEvent.BECAMEEDGE:
			eventDescription = "BECAMEEDGE";
			break;
		default:
			eventDescription = "UNKNOWN RENDEZVOUS EVENT";
		}
		System.out.println(new Date().toString() + "  Rdv: event=" + eventDescription + " from peer = " + event.getPeer());

		if(connected && this.appPGRdvService.isConnectedToRendezVous()) {
//			if (rdvLock.availablePermits() <= 0) {
			rdvLock.release();
//			}
		}
	}


	/**
	 * The caller to this function blocks until the peer is connected to
	 * a rendezvous peer.
	 */
	public void waitForRdv() throws InterruptedException {
		rdvLock.acquire();
		rdvLock.release();
	}

	// ---------------------------------

	/**
	 * Setup a propagate pipe
	 * TODO: remove this function. just required for testing
	 */
	private void setupPipe() {
		PipeAdvertisement propagatePipeAdv = (PipeAdvertisement )AdvertisementFactory.
		newAdvertisement(PipeAdvertisement.getAdvertisementType());

		try {
			byte[] bid  = MessageDigest.getInstance("MD5").digest("abcd".getBytes("ISO-8859-1"));
			PipeID pipeID = IDFactory.newPipeID(appPeerGroup.getPeerGroupID(), bid);
			propagatePipeAdv.setPipeID(pipeID);
			propagatePipeAdv.setType(PipeService.PropagateType);
			propagatePipeAdv.setName("A chattering propagate pipe");
			propagatePipeAdv.setDescription("verbose description");

			PipeService pipeService = appPeerGroup.getPipeService();
			inputPipe  = pipeService.createInputPipe(propagatePipeAdv, this);
			outputPipe = pipeService.createOutputPipe(propagatePipeAdv, 1000);
			System.out.println("Propagate pipes and listeners created");
			System.out.println("Propagate PipeID: " + pipeID.toString());
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}

	// ---------------------------------

	/**
	 * Send a dummy message to all peers connected to the propagate pipe
	 * TODO: remove this function. just required for testing
	 */
	private void sendToPeers() {
		try {
			Message msg = new Message();

			MessageElement fromElem = new ByteArrayMessageElement(
					"From", null, appPeerGroup.getPeerName().getBytes("ISO-8859-1"), null
			);

			MessageElement fromIDElem = new ByteArrayMessageElement(
					"FromID", null, this.myPeerID.getBytes("ISO-8859-1"), null
			);

			MessageElement msgElem = new ByteArrayMessageElement(
					"Msg", null, (new Date()).toString().getBytes("ISO-8859-1"), null
			);

			msg.addMessageElement(fromElem);
			msg.addMessageElement(fromIDElem);
			msg.addMessageElement(msgElem);
			outputPipe.send(msg);
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	// ---------------------------------

	/**
	 * 1.) Setup a propagate pipe. See {@link #setupPipe()}
	 * 2.) Send dummy pessages to all peers connected to the pipe. {@link #sendToPeers()}
	 * TODO: remove this function. just required for testing
	 */
	private void doSomething() {
		setupPipe();
		new Thread("AppGroup Send Thread") {
			public void run() {
				int sleepy=10000;
				while(true) {
					sendToPeers();
					try {
						sleep(sleepy);
					}
					catch(InterruptedException e) {}
				}
			}
		}.start();
	}

	// ---------------------------------

	// the InputPipe callback

	/**
	 * Receives messages from other peers.
	 * TODO: remove this function. just required for testing
	 * 
	 * @see PipeEventListener#pipeEvent(int)
	 */
	public void pipeMsgEvent(PipeMsgEvent event) {
		try {
			Message msg = event.getMessage();
			byte[] msgBytes = msg.getMessageElement("Msg").getBytes(true);  
			byte[] fromBytes = msg.getMessageElement("From").getBytes(true);
			byte[] fromIDBytes = msg.getMessageElement("FromID").getBytes(true); 

			String fromID = new String(fromIDBytes);
			String fromPeerName = new String(fromBytes);
			if(fromID.equals(this.myPeerID)) {
				System.out.print("(from self): ");
			}
			else {
				System.out.print("(from other): ");
			}
			System.out.print(new Date());
			System.out.println(" " + fromPeerName + " says " + new String(msgBytes));
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}

	}

	// ---------------------------------

	/**
	 * Function to clean the JXTA cm cache on peer startup.
	 */
	private static void clearCache(final File rootDir) {
		try {
			if (rootDir.exists()) {
				File[] list = rootDir.listFiles();
				for (File aList : list) {
					if (aList.isDirectory()) {
						clearCache(aList);
					} else {
						aList.delete();
					}
				}
			}
			rootDir.delete();
			System.out.println("Cache component " + rootDir.toString() + " cleared.");
		}
		catch (Throwable t) {
			System.out.println("Unable to clear " + rootDir.toString());
			t.printStackTrace();
		}
	}

	/**
	 * FIXME: needs to be re-implemented
	 */
	public void setMode(ConfigMode mode) {
//		// change the config-mode
//		try {
//		this.manager.setMode(mode);
//		} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
	}

	public PeerGroup getPeerGroup() {
		return this.appPeerGroup;
	}

	/* =========================================================
	 * IP2PMANAGER FUNCTIONS 
	 * ========================================================= */
	/**
	 * @see IP2PManager#getPeerID()
	 */
	public String getPeerID() {
		return this.appPeerGroup.getPeerID().getUniqueValue().toString();
	}

	/**
	 * @see IP2PManager#getPeerName()
	 */
	public String getPeerName() {
		return this.appPeerGroup.getPeerName();
	}

	/**
	 * @see IP2PManager#getGroupID()
	 */
	public String getGroupID() {
		return this.appPeerGroup.getPeerGroupID().getUniqueValue().toString();
	}

	/**
	 * @see IP2PManager#getGroupName()
	 */
	public String getGroupName() {
		return this.appPeerGroup.getPeerGroupName();
	}

	/**
	 * @see IP2PManager#getPeerList()
	 */
	public List<String> getPeerList() {
		List<String> peers = new ArrayList<String>();

		List<PeerAdvertisement> peerAdvs = this.getPeerAdvertisements();
		for (PeerAdvertisement peerAdv : peerAdvs) {
			peers.add(peerAdv.getName());
		}

		return peers;
	}	

	/**
	 * @see IP2PManager#getPeerAdvertisements()
	 */
	public List<PeerAdvertisement> getPeerAdvertisements() {
		List<PeerAdvertisement> peerAdvs = new ArrayList<PeerAdvertisement>();

		try {			
			// obtain the the discovery service
			DiscoveryService discoSvc = this.appPeerGroup.getDiscoveryService();
			discoSvc.getRemoteAdvertisements(null, DiscoveryService.PEER, null, null, 1000);
			discoSvc.addDiscoveryListener(this);
			
			// sleep for a moment
			Thread.sleep(300);

			// Enumeration<Advertisement> advs = discoSvc.getLocalAdvertisements(DiscoveryService.PEER, null, null);
			Enumeration<Advertisement> advs = discoSvc.getLocalAdvertisements(DiscoveryService.PEER, "Name", "*");
			if (advs != null) {
				while (advs.hasMoreElements()) {
					Advertisement adv=advs.nextElement();
					if (adv instanceof PeerAdvertisement) {
						peerAdvs.add((PeerAdvertisement) adv);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		

		return peerAdvs;
	}	

	/* =========================================================
	 * DISCOVERY-LISTENER FUNCTIONS 
	 * ========================================================= */
	/**
	 * @see DiscoveryListener#discoveryEvent(DiscoveryEvent)
	 */
	public void discoveryEvent(DiscoveryEvent event) {
		System.out.println(event.toString());
	}
}
