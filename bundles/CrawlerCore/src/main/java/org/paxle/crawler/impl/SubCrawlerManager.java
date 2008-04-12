package org.paxle.crawler.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceReference;
import org.paxle.core.prefs.Properties;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

public class SubCrawlerManager implements ISubCrawlerManager {
	private static final String DISABLED_PROTOCOLS = ISubCrawlerManager.class.getName() + "." + "disabledProtocols";
	
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A {@link HashMap} containing the protocol that is supported by the sub-crawler as key and
	 * the {@link ServiceReference} as value.
	 */
	private HashMap<String, ISubCrawler> subCrawlerList = new HashMap<String, ISubCrawler>();

	/**
	 * A list of disabled protocols
	 */
	private Set<String> disabledProtocols = new HashSet<String>();
	
	/**
	 * The properties of this component
	 */
	private Properties props = null;
	
	public SubCrawlerManager(Properties props) {
		this.props = props;
		if (this.props != null && this.props.containsKey(DISABLED_PROTOCOLS)) {
			this.disabledProtocols = this.props.getSet(DISABLED_PROTOCOLS);
		}
	}
	
	/**
	 * Adds a newly detected {@link ISubCrawler} to the {@link Activator#subCrawlerList subcrawler-list}
	 * @param protocols the protocols supported by the crawler
	 * @param reference the reference to the deployed {@link ISubCrawler subcrawler-service}
	 */
	public void addSubCrawler(String[] protocols, ISubCrawler subCrawler) {
		if (protocols == null) throw new NullPointerException("The protocol array must not be null.");
		for (String protocol : protocols) this.addSubCrawler(protocol, subCrawler);
	}
	
	private void addSubCrawler(String protocol, ISubCrawler subCrawler) {
		if (protocol == null) throw new NullPointerException("The protocol must not be null");
		if (subCrawler == null) throw new NullPointerException("The crawler object must not be null");		
		protocol = protocol.toLowerCase();
		
		this.subCrawlerList.put(protocol, subCrawler);		
		this.logger.info(String.format("Crawler for protocol '%s' was installed.",protocol));
	}
	
	/**
	 * Removes a uninstalled {@link ISubCrawler} from the {@link Activator#subCrawlerList subcrawler-list}
	 * @param protocols the protocols supported by the crawler that should be uninstalled
	 */
	public void removeSubCrawler(String[] protocols) {
		if (protocols == null) throw new NullPointerException("The protocol array must not be null.");
		for (String protocol : protocols) this.removeSubCrawler(protocol);
	}	
	
	public void removeSubCrawler(String protocol) {
		if (protocol == null) throw new NullPointerException("The protocol must not be null");		
		protocol = protocol.toLowerCase();
		
		this.subCrawlerList.remove(protocol);
		this.logger.info(String.format("Crawler for protocol '%s' was uninstalled.",protocol));
	}		
	
	/**
	 * Getting a {@link ISubCrawler} which is capable to handle
	 * the given network-protocol
	 * @param protocol
	 * @return the requested sub-crawler or <code>null</code> if no crawler for
	 *         the specified protocol is available
	 */
	public ISubCrawler getSubCrawler(String protocol) {
		if (protocol == null) return null;
		protocol = protocol.toLowerCase();
		
		if (this.disabledProtocols.contains(protocol)) return null;
		return this.subCrawlerList.get(protocol);
	}	
	
	/**
	 * Determines if a given protocol is supported by one of the registered
	 * {@link ISubCrawler sub-crawlers}.
	 * @param protocol the protocol
	 * @return <code>true</code> if the given protocol is supported or <code>false</code> otherwise
	 */
	public boolean isSupported(String protocol) {
		if (protocol == null) return false;
		protocol = protocol.toLowerCase();
		
		if (this.disabledProtocols.contains(protocol)) return false; 
		return this.subCrawlerList.containsKey(protocol);
	}

	/**
	 * @see ISubCrawlerManager#getSubCrawlers()
	 */
	public Collection<ISubCrawler> getSubCrawlers() {
		return Collections.unmodifiableCollection(subCrawlerList.values());
	}
	
	/**
	 * @see ISubCrawler#getProtocols()
	 */
	public Collection<String> getProtocols() {
		Set<String> keySet = this.subCrawlerList.keySet();
		String[] keyArray = keySet.toArray(new String[keySet.size()]);
		return Collections.unmodifiableCollection(Arrays.asList(keyArray));
	}

	/**
	 * @see ISubCrawlerManager#disableProtocol(String)
	 */
	public void disableProtocol(String protocol) {
		if (protocol == null) return;
		protocol = protocol.toLowerCase();
		
		this.disabledProtocols.add(protocol);
		if (this.props != null) this.props.setSet(DISABLED_PROTOCOLS, this.disabledProtocols);
	}

	/**
	 * @see ISubCrawlerManager#enableProtocol(String)
	 */
	public void enableProtocol(String protocol) {
		if (protocol == null) return;
		protocol = protocol.toLowerCase();
		
		this.disabledProtocols.remove(protocol);		
		if (this.props != null) this.props.setSet(DISABLED_PROTOCOLS, this.disabledProtocols);
	}

	/**
	 * @see ISubCrawlerManager#disabledProtocols()
	 */
	public Set<String> disabledProtocols() {
		return Collections.unmodifiableSet(this.disabledProtocols);
	}
}
