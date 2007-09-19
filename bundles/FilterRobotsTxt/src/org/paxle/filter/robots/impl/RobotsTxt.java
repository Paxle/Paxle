package org.paxle.filter.robots.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RobotsTxt implements Serializable {
	
	private boolean accessRestricted = false;
	private String downloadStatus = "200 OK";
	private String hostPort = null;
	private Date loadedDate = null;
	private HashMap<String, RuleBlock> ruleBlockMap = new HashMap<String, RuleBlock>();
	private List<RuleBlock> ruleBlocks = new ArrayList<RuleBlock>();	
	
	public RobotsTxt(String hostPort, Date loadedDate, String downloadStatus) {
		this(hostPort,loadedDate,downloadStatus, false);
	}
	
	public RobotsTxt(String hostPort, Date loadedDate, String downloadStatus, boolean accessRestricted) {
		this.hostPort = hostPort;
		this.loadedDate = loadedDate;
		this.downloadStatus = downloadStatus;
		this.accessRestricted = accessRestricted;
	}
	
	public String getHostPort() {
		return this.hostPort;
	}
	
	public Date getLoadedDate() {
		return this.loadedDate;
	}
	
	void addRuleBlock(RuleBlock ruleBlock) {
		this.ruleBlocks.add(ruleBlock);
		if (this.ruleBlockMap.size() > 0) this.ruleBlockMap.clear();
	}
	
	public boolean isDisallowed(String agent, String path) {
		if (path != null && path.equals("/robots.txt")) return Boolean.FALSE;
		if (this.accessRestricted) return Boolean.TRUE;		
		if (this.ruleBlocks == null || this.ruleBlocks.size() == 0) return Boolean.FALSE;
		
		if (this.ruleBlockMap.size() == 0) this.buildRuleBlockMap();
		
		RuleBlock ruleBlock = null;
		if (this.ruleBlockMap.containsKey(agent)) {
			ruleBlock = ruleBlockMap.get(agent);
		} else if (this.ruleBlockMap.containsKey("*")) {
			ruleBlock = ruleBlockMap.get("*");
		}
		
		if (ruleBlock != null) {
			Boolean isDisallowed = ruleBlock.isDisallowed(agent, path);
			if (isDisallowed != null) return isDisallowed;
		}
		
		return Boolean.FALSE;
	}
	
	private void buildRuleBlockMap() {
		for (RuleBlock ruleBlock : this.ruleBlocks) {
			for (String agent : ruleBlock.getAgents()) {
				this.ruleBlockMap.put(agent, ruleBlock);
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		
		str.append("# Robots.txt of host: ").append(this.hostPort).append("\r\n")
		   .append("# Downloaded date: ").append(this.loadedDate).append("\r\n")
		   .append("# Download status: ").append(this.downloadStatus).append("\r\n")
		   .append("\r\n");
		
		if (this.accessRestricted) {
			str.append("User-agent: *\r\n")
			   .append("Disallow: /\r\n");		
		} else if (this.ruleBlocks.size() == 0) {
			str.append("User-agent: *\r\n")
			   .append("Disallow: \r\n");				
		} else {
			for (RuleBlock ruleBlock : this.ruleBlocks) {
				str.append(ruleBlock).append("\r\n");
			}			
		}
		
		return str.toString();
	}
}
