package org.paxle.gui.remote.actions;

import java.net.URL;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.JavaTypeMapper;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionSupport;

public class ListPeerConfigAction extends ActionSupport {
	private static final long serialVersionUID = 1L;
	
	private JsonFactory jf = new JsonFactory(); 
	private JavaTypeMapper jtm=new JavaTypeMapper();
	
	private String peerUrl = null;
	private Long bundleId = null;
	private String pid =  null;
	
	private Object peerConfigData = null;
	
	@Override
	public String execute() throws Exception {
		StringBuffer configURL = new StringBuffer(this.peerUrl + "/configurations");
		if (this.bundleId != null) configURL.append("/" + bundleId);
		if (this.pid != null) configURL.append("/" + pid );
		
		this.peerConfigData = jtm.read(jf.createJsonParser(new URL(configURL.toString())));
		return Action.SUCCESS;
	}
	
	public Object getPeerConfigData() {
		return this.peerConfigData;
	}

	public String getPeerUrl() {
		return peerUrl;
	}

	public void setPeerUrl(String peerUrl) {
		this.peerUrl = peerUrl;
	}
	
	public Long getBundleId() {
		return bundleId;
	}

	public void setBundleId(Long bundleId) {
		this.bundleId = bundleId;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}	
}
