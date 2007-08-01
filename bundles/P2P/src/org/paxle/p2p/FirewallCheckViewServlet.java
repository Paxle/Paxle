package org.paxle.p2p;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.jxta.platform.NetworkManager;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
import org.paxle.p2p.impl.P2PManager;

public class FirewallCheckViewServlet extends VelocityViewServlet {
	private static final long serialVersionUID = 1L;
	private P2PManager p2pManager = null;
	
	public FirewallCheckViewServlet(P2PManager p2pManager) {
		this.p2pManager = p2pManager;
	}	
	
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context ) {

		context.put("P2Pmanager", this.p2pManager); 	
		context.put("P2Pmode_rendezvous_relay",NetworkManager.ConfigMode.RENDEZVOUS_RELAY);
		
		Template template = null;   
		try{
			template = Velocity.getTemplate("/resources/templates/firewallcheck.vm");
		} catch( Exception e ) {
			System.err.println("Exception caught: " + e.getMessage());
		}
		//TODO: set not firewalled.

		return template;
	}
}
