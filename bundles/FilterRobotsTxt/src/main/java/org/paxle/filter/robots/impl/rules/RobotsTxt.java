package org.paxle.filter.robots.impl.rules;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RobotsTxt implements Serializable {
	
	public static final long RELOAD_INTERVAL_DEFAULT = TimeUnit.SECONDS.toMillis(7*24*60*60);
	/** The reload interval used if an error occurred, which may last for some time */
	public static final long RELOAD_INTERVAL_ERROR = TimeUnit.SECONDS.toMillis(1*24*60*60);
	/** The reload interval used if an error occurred, which is probably only a temporary problem */
	public static final long RELOAD_INTERVAL_TEMP_ERROR = TimeUnit.SECONDS.toMillis(1*24*60*60);
	
	private boolean accessRestricted = false;
	private String downloadStatus = "200 OK";
	private String hostPort = null;
	private Date loadedDate = null;
	private long reloadInterval = RELOAD_INTERVAL_DEFAULT;	
	
	/**
	 * A map containing the agent-name as key and the {@link RuleBlock} for the given 
	 * agent as value
	 */
	private HashMap<String, RuleBlock> ruleBlockMap = new HashMap<String, RuleBlock>();
	
	/**
	 * A list of all {@link RuleBlock rule-blocks} found in the robots.txt file
	 */
	private List<RuleBlock> ruleBlocks = new ArrayList<RuleBlock>();
	
	/**
	 * A list of sitemaps found in the robots.txt file
	 */
	private List<URI> sitemaps = new ArrayList<URI>();
	
	public RobotsTxt(String hostPort, long reloadInterval, String downloadStatus) {
		this(hostPort, reloadInterval, downloadStatus, false);
	}
	
	public RobotsTxt(String hostPort, long reloadInterval, String downloadStatus, boolean accessRestricted) {
		this.hostPort = hostPort;
		this.loadedDate = new Date();
		this.reloadInterval = (reloadInterval < 0) ? RELOAD_INTERVAL_DEFAULT : reloadInterval;
		this.downloadStatus = downloadStatus;
		this.accessRestricted = accessRestricted;
	}
	
	/** Return the hostname and possibly the port for this robots.txt definition, e.g. "www.example.org:888" */
	public String getHostPort() {
		return this.hostPort;
	}
	
	public Date getLoadedDate() {
		return this.loadedDate;
	}
	
	/**
	 * Return the date when the cache robots.txt file expires.
	 * @return Date the cached file expires
	 */
	public Date getExpirationDate() {
		return new Date(this.loadedDate.getTime() + this.getReloadInterval());
	}
	
	/**
	 * Returns the reload interval of this file in milliseconds
	 * @return
	 */
	public long getReloadInterval() {
		return this.reloadInterval;
	}
	
	public void addRuleBlock(RuleBlock ruleBlock) {
		this.ruleBlocks.add(ruleBlock);
		if (this.ruleBlockMap.size() > 0) this.ruleBlockMap.clear();
	}
	
	public void addSitemap(URI sitemapURI){
		if (sitemapURI == null) return;
		this.sitemaps.add(sitemapURI);
	}
	
	/**
	 * @return the list of sitemap-URI found in the robots.txt file
	 */
	public Collection<URI> getSitemaps() {
		return Collections.unmodifiableCollection(this.sitemaps);
	}
	
	/**
	 * @return the amount of rule-blocks contained in this robots.txt file
	 */
	public int size() {
		return this.ruleBlocks.size();
	}
	
	/**
	 * @param idx
	 * @return the {@link RuleBlock} for the given idx.
	 */
	public RuleBlock getRuleBlock(int idx) {
		return (this.ruleBlocks.size() < idx) ? null : this.ruleBlocks.get(idx);
	}
	
	
	/**
	 * Get the {@link RuleBlock} for the given agent
	 * @param agent
	 * @return the found {@link RuleBlock} or <code>null</code> if no block was found
	 */
	public RuleBlock getRuleBlock(String agent) {
		if (this.ruleBlocks == null || this.ruleBlocks.size() == 0) return null;
		
		if (this.ruleBlockMap.size() == 0) this.buildRuleBlockMap();
		
		RuleBlock ruleBlock = null;
		if (this.ruleBlockMap.containsKey(agent)) {
			ruleBlock = ruleBlockMap.get(agent);
		} else if (this.ruleBlockMap.containsKey("*")) {
			ruleBlock = ruleBlockMap.get("*");
		}
		return ruleBlock;
	}
	
	public boolean isDisallowed(String agent, String path) {
		if (path != null && path.equals("/robots.txt")) return false;
		if (this.accessRestricted) return true;		
		if (this.ruleBlocks == null || this.ruleBlocks.size() == 0) return false;
		
		if (this.ruleBlockMap.size() == 0) this.buildRuleBlockMap();
		
		RuleBlock ruleBlock = null;
		if (this.ruleBlockMap.containsKey(agent)) {
			ruleBlock = ruleBlockMap.get(agent);
		} else if (this.ruleBlockMap.containsKey("*")) {
			ruleBlock = ruleBlockMap.get("*");
		}
		
		if (ruleBlock != null) {
			Boolean isDisallowed = ruleBlock.isDisallowed(agent, path);
			if (isDisallowed != null) return isDisallowed.booleanValue();
		}
		
		return false;
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
		
		str.append(String.format("# Robots.txt of host: %s \r\n",this.hostPort))
		   .append(String.format("# Downloaded date: %s \r\n", this.loadedDate))
		   .append(String.format("# Download status: %s \r\n", this.downloadStatus))
		   .append(String.format("# Expiration date: %s \r\n", this.getExpirationDate()))
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
		
		if (this.sitemaps.size() > 0) {
			str.append("\r\n")
			   .append("# Sitemaps\r\n");
			for (URI sitemap : this.sitemaps) {
				str.append("Sitemap: ").append(sitemap.toASCIIString()).append("\r\n");
			}
		}
		
		return str.toString();
	}
}
