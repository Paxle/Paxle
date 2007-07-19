package org.paxle.crawler.impl;

import java.util.Collection;
import java.util.HashMap;

import org.osgi.framework.ServiceReference;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

public class SubCrawlerManager implements ISubCrawlerManager {
	
	/**
	 * A {@link HashMap} containing the protocol that is supported by the sub-crawler as key and
	 * the {@link ServiceReference} as value.
	 */
	private HashMap<String, ISubCrawler> subCrawlerList = new HashMap<String, ISubCrawler>();

	/**
	 * Adds a newly detected {@link ISubCrawler} to the {@link Activator#subCrawlerList subcrawler-list}
	 * @param reference the reference to the deployed {@link ISubCrawler subcrawler-service}
	 */
	public void addSubCrawler(String protocol, ISubCrawler subCrawler) {
		this.subCrawlerList.put(protocol, subCrawler);		
		System.out.println("Crawler for protocol '" + protocol + "' was installed.");
	}
	
	/**
	 * Removes a uninstalled {@link ISubCrawler} from the {@link Activator#subCrawlerList subcrawler-list}
	 * @param reference the reference to the uninstalled {@link ISubCrawler subcrawler-service}
	 */
	public void removeSubCrawler(String protocol) {
		this.subCrawlerList.remove(protocol);
		System.out.println("Crawler for protocol '" + protocol + "' was uninstalled.");
	}	
	
	/**
	 * Getting a {@link ISubCrawler} which is capable to handle
	 * the given network-protocol
	 * @param protocol
	 * @return
	 */
	public ISubCrawler getSubCrawler(String protocol) {
		return this.subCrawlerList.get(protocol);
	}	
	
	/**
	 * Determines if a given protocol is supported by one of the registered
	 * {@link ISubCrawler sub-crawlers}.
	 * @param protocol the protocol
	 * @return <code>true</code> if the given protocol is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String protocol) {
		return this.subCrawlerList.containsKey(protocol);
	}

	/**
	 * @see ISubCrawlerManager#getSubCrawlers()t
	 */
	public Collection<ISubCrawler> getSubCrawlers() {
		return subCrawlerList.values();
	}
}
