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
import java.util.Random;

import net.jxta.credential.AuthenticationCredential;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.Advertisement;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.document.StructuredDocumentFactory;
import net.jxta.document.XMLElement;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.endpoint.MessageElement;
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

import org.paxle.p2p.IP2PManager;

public class P2PManager extends Thread implements IP2PManager, RendezvousListener, PipeMsgListener {

	/* ==============================================================
	 * JXTA Netpeer group constants
	 * ============================================================== */    
	private static final String NETPG_GID = "urn:jxta:uuid-0345A1D0CDB24759854AC5FA4597B7B502";
	private static final String NETPG_NAME = "Paxle NetPG";
	private static final String NETPG_DESC = "A Private Paxle Net Peer Group";
	
	/* ==============================================================
	 * JXTA Application group constants
	 * ============================================================== */
    private static final String APPGRP_NAME = "MyAppGroup";
    private static final String APPGRP_DESC = "MyAppGroup Description goes here";
    private static final String APPGRP_GID =  "urn:jxta:uuid-79B6A084D3264DF8B641867D926C48D902";
    private static final String APPGRP_SPECID = "urn:jxta:uuid-309B33F10EDF48738183E3777A7C3DE9C5BFE5794E974DD99AC7D409F5686F3306";
    
	private DiscoveryService netPGDiscoveryService;
	private RendezVousService appPGRdvService;
	private RendezVousService netPGRdvService;
	private NetworkConfigurator configurator;
	private OutputPipe outputPipe;
	private InputPipe inputPipe;
	private PeerGroup netPeerGroup;
	private PeerGroup appPeerGroup;
	private Random rand;


	private String jxtaHome;
	private String rdvlock = new String("rocknroll");
	private String exitlock = new String("jazz");
	private String myPeerID;

	private boolean connected=false;	

	private URI seedingURI = null;
	
	public P2PManager(File home, URI seedURI) throws IOException {
		jxtaHome = home.getCanonicalPath();
		this.seedingURI = seedURI;
		rand = new Random();
		
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
			this.doSomething();
			this.waitForQuit();
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

	         myPeerID = appPeerGroup.getPeerID().toString();

	         netPGDiscoveryService.publish(implAdv);
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

	   // -----------------------------------

	   private void stopit() {
	      netPeerGroup.stopApp();
	   }

	   // -----------------------------------

	   private void configureJXTA() {
	      configurator = new NetworkConfigurator();
	      configurator.setHome(new File(jxtaHome));
	      configurator.setPeerID(IDFactory.newPeerID(PeerGroupID.defaultNetPeerGroupID));
	      configurator.setName(P2PTools.getComputerName());
	      configurator.setPrincipal("ofno");
	      configurator.setPassword("consequence");
	      configurator.setDescription("I am a P2P Peer.");
	      configurator.setUseMulticast(false);

	      // fetch seeds from file, or alternately from network 
//	      URI seedingURI = new File("seeds.txt").toURI();  
	      configurator.addRdvSeedingURI(seedingURI);
	      configurator.addRelaySeedingURI(seedingURI);
	      configurator.setUseOnlyRelaySeeds(true);
	      configurator.setUseOnlyRendezvousSeeds(true);

	      configurator.setTcpIncoming(false);

	      try {
	         configurator.save();
	      }
	      catch(Throwable e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
	      System.out.println("Platform configured and saved");
	   }

	   // ---------------------------------

	   // the Rendezvous service callback
	   public void rendezvousEvent(RendezvousEvent event) {
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

	      synchronized(rdvlock) {
	         if( connected ) {
	            rdvlock.notify();
	         }
	      }
	   }

	   // ---------------------------------

	   public void waitForRdv() {
	      synchronized (rdvlock) {
	         while (! appPGRdvService.isConnectedToRendezVous() ) {
	            System.out.println("Awaiting rendezvous conx...");
	            try {
	               if (! appPGRdvService.isConnectedToRendezVous() ) {
	                  rdvlock.wait();
	               }
	            }
	            catch (InterruptedException e) {
	               ;
	            }
	         }
	      }
	   }

	   // ---------------------------------

	   private void waitForQuit() {
	      synchronized(exitlock) {
	         try {
	            System.out.println("waiting for quit");
	            exitlock.wait();
	            System.out.println("Goodbye");
	         }
	         catch(InterruptedException e) {
	            ;
	         }
	      }
	   }

	   // ---------------------------------

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

	   private void sendToPeers() {
	      try {
	         String data = new Integer(rand.nextInt()).toString();
	         Message msg = new Message();

	         MessageElement fromElem = new ByteArrayMessageElement(
	            "From", null, myPeerID.toString().getBytes("ISO-8859-1"), null
	         );
	         MessageElement msgElem = new ByteArrayMessageElement(
	            "Msg", null, data.getBytes("ISO-8859-1"), null
	         );

	         msg.addMessageElement(fromElem);
	         msg.addMessageElement(msgElem);
	         outputPipe.send(msg);
	      }
	      catch(IOException e) {
	         e.printStackTrace();
	      }

	   }

	   // ---------------------------------

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
	   public void pipeMsgEvent(PipeMsgEvent event) {
	     try {
	       Message msg = event.getMessage();
	       byte[] msgBytes = msg.getMessageElement("Msg").getBytes(true);  
	       byte[] fromBytes = msg.getMessageElement("From").getBytes(true);  

	       String fromPeerID = new String(fromBytes);
	       if( fromPeerID.equals(myPeerID)) {
	          System.out.print("(from self): ");
	       }
	       else {
	          System.out.print("(from other): ");
	       }
	       System.out.print(new Date());
	       System.out.println(" " + fromPeerID + " says " + new String(msgBytes));
	     }
	     catch (Exception e) {
	       e.printStackTrace();
	       return;
	     }

	   }

	   // ---------------------------------

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

	   // ---------------------------------	
	
	public String[] getPeerList() {
		List<String> peers = new ArrayList<String>();
		
		try {			
			// obtain the the discovery service
			DiscoveryService discoSvc = this.appPeerGroup.getDiscoveryService();
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
//		return new String[0];
	}	
	
	public void setMode(ConfigMode mode) {
//		// change the config-mode
//		try {
//			this.manager.setMode(mode);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	/* =========================================================
	 * Functions for IP2PManager 
	 * ========================================================= */
	
	public String getPeerID() {
		return this.appPeerGroup.getPeerID().getUniqueValue().toString();
	}
	
	public PeerGroup getPeerGroup() {
		return this.appPeerGroup;
	}
	
	public String getPeerName() {
		return this.appPeerGroup.getPeerName();
	}
		
	public String getGroupID() {
		return this.appPeerGroup.getPeerGroupID().getUniqueValue().toString();
	}
	
	public String getGroupName() {
		return this.appPeerGroup.getPeerGroupName();
	}
}
