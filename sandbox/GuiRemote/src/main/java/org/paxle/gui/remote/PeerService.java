package org.paxle.gui.remote;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.JavaTypeMapper;

public class PeerService {
	/**
	 * TODO: this should be configurable later
	 */
	private static final String[][] PROPS = new String[][] {
		{"org.paxle.lucene-db","docs.known"},
		{"org.paxle.crawler","ppm"}
	};	
	
	private JsonFactory jf = new JsonFactory(); 
	private JavaTypeMapper jtm=new JavaTypeMapper();
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A list of all known peers
	 */
	private static final List<Peer> peers = new ArrayList<Peer>();
	
	public PeerService() {
		Peer peer1 = new Peer();
		peer1.setName("Pinky");
		peer1.setAddress("http://192.168.10.199:8282");
		peers.add(peer1);			
	}
	
	public List<Peer> getPeers() {
		return peers;
	}
	
	public void addPeer(Peer peer) {
		peers.add(peer);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void updateStatus() {
		if (true == true) return;
		this.logger.info("Update status ...");
		for (Peer peer: peers) {
			try {
				URL peerUrl = new URL(peer.getAddress() + "/monitorables");
				List<?> monitorables = (List<?>) jtm.read(jf.createJsonParser(peerUrl));
				long timeStamp = System.currentTimeMillis();
				
//				for (String[] prop : PROPS) {
//					String monitorableId = prop[0];
//					String varName = prop[1];
//					
//					Object value = data.get(monitorableId).get(varName).get("value");					
//					
//					if (value instanceof Number) {
//						peer.setPeerStatus(monitorableId + "/" + varName, timeStamp, (Number) value);
//					}
//				}
			} catch (Exception e) {
				this.logger.equals(e);
			}
		}
	}
}
