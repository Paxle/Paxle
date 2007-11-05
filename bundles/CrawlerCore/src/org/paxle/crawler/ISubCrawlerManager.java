package org.paxle.crawler;

import java.util.Collection;

public interface ISubCrawlerManager {
	/**
	 * @return an unmodifiable collection of all installed {@link ISubCrawler sub-crawlers}
	 */
	public Collection<ISubCrawler> getSubCrawlers();
	
	/**
	 * @return an unmodifiable collection of all protocols supported by the registered {@link ISubCrawler sub-crawlers}
	 */
	// FIXME: public Collection<String> getProtocols();
}
