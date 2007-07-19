package org.paxle.crawler.impl;

import java.net.URL;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;
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
	protected void execute(ICommand command) {
		try {
			// get the URL to crawl
			String urlString = command.getLocation();
			URL url = new URL(urlString);

			// get a sub-crawler that is capable to handle the specified protocol
			String protocol = url.getProtocol();
			ISubCrawler crawler = this.subCrawlerManager.getSubCrawler(protocol);
			if (crawler == null) {
				command.setResult(ICommand.Result.Failure, "No crawler for protocol '" + protocol + "' found.");
				return;
			}			
			
			// pass the URL to the cralwer
			ICrawlerDocument crawlerDoc = crawler.request(urlString);
			if (crawlerDoc == null) {
				// TODO: set the statuscode accordingly
			} else if (crawlerDoc.getStatus() != ICrawlerDocument.Status.OK) {
				
			}
			
			// TODO: mimetype detection
			// resource.getMimeType()
			command.setCrawlerDocument(crawlerDoc);
			
			
		} catch (Exception e) {
			command.setResult(ICommand.Result.Failure, "Unexpected error while crawling the resource");
		}
	}
}
