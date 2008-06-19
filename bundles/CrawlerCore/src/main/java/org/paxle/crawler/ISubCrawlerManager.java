package org.paxle.crawler;

import java.util.Collection;
import java.util.Set;

public interface ISubCrawlerManager {
	/**
	 * @return an unmodifiable collection of all installed {@link ISubCrawler sub-crawlers}
	 */
	public Collection<ISubCrawler> getSubCrawlers();
	
	/**
	 * @return a list of known but disabled protocols
	 */
	public Set<String> disabledProtocols();
	
	/**
	 * Disable crawling using a given protocol
	 * @param protocol the protocol to disable 
	 */
	public void enableProtocol(String protocol);
	
	/**
	 * Enables crawling using a given protocol
	 * @param protocol the protocol to enable
	 */
	public void disableProtocol(String protocol);
	
	/**
	 * @return an unmodifiable collection of all protocols supported by the registered {@link ISubCrawler sub-crawlers}
	 */
	public Collection<String> getProtocols();
	
	/**
	 * Determines if a given protocol is supported by one of the registered
	 * {@link ISubCrawler sub-crawlers}.
	 * @param protocol the protocol
	 * @return <code>true</code> if the given protocol is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String protocol);
}
