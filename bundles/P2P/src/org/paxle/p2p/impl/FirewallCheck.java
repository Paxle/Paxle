package org.paxle.p2p.impl;


import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.paxle.p2p.FirewallCheckViewServlet;
import org.paxle.p2p.IFirewallCheck;

public class FirewallCheck implements IFirewallCheck {
	private BundleContext bc;
	private HttpService http;
	private int firewalled=NOT_TESTED;
	private long timeout=0;
	
	public FirewallCheck(BundleContext bc){
		this.bc=bc;
		//getting a reference to the osgi http service
		ServiceReference sr = this.bc.getServiceReference(HttpService.class.getName());
		if(sr != null) {
			// getting the http service
			http = (HttpService)this.bc.getService(sr);
			if(http != null) {				
				// registering the servlet which will be accessible using 
				try {
					http.registerServlet("/paxle/firewallcheck", new FirewallCheckViewServlet(), null, null);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		
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
		//TODO: get IP, send a known peer a jxta message to ping the ip/port
		//TODO: if there is no peer (or we have no p2p implemented yet), ping webservice,
		//TODO: which should pong us on /paxle/firewallcheck
		this.timeout=System.currentTimeMillis()/1000 +timeout;
		//this.firewalled=CHECKING;
		HttpMethod method=new GetMethod("http://test.laxu.de/firewallcheck.php?port=8080");
		HttpClient client=new HttpClient();
		try {
			int status = client.executeMethod(method);
		} catch (HttpException e) {
			firewalled=NOT_TESTED;
		} catch (IOException e) {
			firewalled=NOT_TESTED;
		}
	}

}
