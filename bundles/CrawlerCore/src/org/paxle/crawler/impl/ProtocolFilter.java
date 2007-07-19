package org.paxle.crawler.impl;

import org.paxle.core.filter.IFilter;
import org.paxle.core.queue.ICommand;
import org.paxle.crawler.ISubCrawler;

/**
 * Filters {@link ICommand commands} out if the protocol of the
 * resource is not supported by one of the available {@link ISubCrawler sub-crawlers}
 */
public class ProtocolFilter implements IFilter {

	private SubCrawlerManager subCrawlerManager = null;
	
	public ProtocolFilter(SubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
	}
	
	/**
	 * @see IFilter#filter(ICommand)
	 */
	public void filter(ICommand command) {
		// TODO get the network protocol of the URL
		String protocol = null;
		
		// check if the protocol is supported by one of the 
		// available sub-crawlers
		if (!this.subCrawlerManager.isSupported(protocol)) {
			// TODO: set the statuscode of the command accordingly
		}

	}

}
