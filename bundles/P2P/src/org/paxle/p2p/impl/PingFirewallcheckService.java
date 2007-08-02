package org.paxle.p2p.impl;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;

public class PingFirewallcheckService extends Thread {
	private String serviceURL;
	public PingFirewallcheckService(String url){
		serviceURL=url;
	}
	public void setServiceURL(String url){
		serviceURL=url;
	}
	public void run() {
		//this.firewalled=CHECKING;
		HttpMethod method=new GetMethod(serviceURL);
		HttpClient client=new HttpClient();
		try {
			int status = client.executeMethod(method);
			/*if (status != 200) {
				//error
			}*/
		} catch (HttpException e) {}
		  catch (IOException e) {}
	}
}
