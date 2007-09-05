package org.paxle.crawler.impl;

import java.net.URL;

import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
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
	public void filter(ICommand command, IFilterContext context) {
		try {
			String location = command.getLocation();
			String protocol = new URL(location).getProtocol();

			// check if the protocol is supported by one of the 
			// available sub-crawlers
			if (!this.subCrawlerManager.isSupported(protocol)) {
				command.setResult(ICommand.Result.Rejected, "Protocol not supported");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
