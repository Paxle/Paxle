package org.paxle.crawler.impl;

import java.net.URL;

import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;
import org.paxle.crawler.ICrawlerDocument;
import org.paxle.crawler.ISubCrawler;


public class CrawlerWorker extends AWorker {
	
	/**
	 * A class to manage {@link ISubCrawler sub-crawlers}
	 */
	private SubCrawlerManager subCrawlerManager = null;
	
	public CrawlerWorker(SubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
	}

	@Override
	protected void execute(ICommand cmd) {
		try {
			// get the URL to crawl
			String urlString = cmd.getLocation();
			URL url = new URL(urlString);

			// get a sub-crawler that is capable to handle the specified protocol
			String protocol = url.getProtocol();
			ISubCrawler crawler = this.subCrawlerManager.getSubCrawler(protocol);
			if (crawler == null) {
				// URL not crawlable
				// TODO: set an errorstatus in the command object
				return;
			}			
			
			// pass the URL to the cralwer
			ICrawlerDocument resource = crawler.request(urlString);
			if (resource == null) {
				// TODO: set the statuscode accordingly
			}
			
			// TODO: mimetype detection
			
			// TODO: copy data to ICommand
			
		} catch (Exception e) {
			// TODO: set error-code to ICommand
			e.printStackTrace();
		}
	}
}
