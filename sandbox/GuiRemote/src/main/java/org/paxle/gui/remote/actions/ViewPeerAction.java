package org.paxle.gui.remote.actions;

import java.net.URL;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.JavaTypeMapper;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public class ViewPeerAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	
	private JsonFactory jf = new JsonFactory(); 
	private JavaTypeMapper jtm=new JavaTypeMapper();
	
	private String peerUrl = null;
	private Object peerData = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public String execute() throws Exception {
		String monitorableUrl = this.peerUrl + "/monitorables";
		this.peerData = jtm.read(jf.createJsonParser(new URL(monitorableUrl)));
		return Action.SUCCESS;
	}
	
	public Object getPeerData() {
		return this.peerData;
	}

	public String getPeerUrl() {
		return peerUrl;
	}

	public void setPeerUrl(String peerUrl) {
		this.peerUrl = peerUrl;
	}
}
