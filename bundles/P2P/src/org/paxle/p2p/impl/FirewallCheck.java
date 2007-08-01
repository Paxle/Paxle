package org.paxle.p2p.impl;


import java.io.IOException;

import net.jxta.platform.NetworkManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.paxle.p2p.IFirewallCheck;

public class FirewallCheck extends Thread implements IFirewallCheck {
	private P2PManager p2pManager = null;
	
	private int firewalled=NOT_TESTED;
	private long timeout=0;
	
	public FirewallCheck(P2PManager p2pManager){
		this.p2pManager=p2pManager;
		this.start();
	}
	
	public int getStatus() {
		if(firewalled==CHECKING)
			if(System.currentTimeMillis()/1000 > timeout)
				firewalled=FIREWALLED;//timeout!
				
		return firewalled;
	}
	public void setFirewalled(boolean isFirewalled){
		if(isFirewalled)
			firewalled=FIREWALLED;
		else
			firewalled=NOT_FIREWALLED;
	}
	

	public void startFirewallCheck(int timeout) {
		this.timeout = timeout;
	}	

	@Override
	public void run() {
		//this.firewalled=CHECKING;
		HttpMethod method=new GetMethod("http://test.laxu.de/firewallcheck.php?port=8080");
		HttpClient client=new HttpClient();
		try {
			int status = client.executeMethod(method);
			if (status == 200) {
				this.p2pManager.setMode(NetworkManager.ConfigMode.RENDEZVOUS_RELAY);
			}
		} catch (HttpException e) {
			firewalled=NOT_TESTED;
		} catch (IOException e) {
			firewalled=NOT_TESTED;
		}
	}
}
