package org.paxle.crawler.impl;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.ISubCrawler;

public class CrawlerWorker extends AWorker<ICommand> {
	
	protected Log logger = LogFactory.getLog(CrawlerWorker.class);
	
	/**
	 * A class to manage {@link ISubCrawler sub-crawlers}
	 */
	private SubCrawlerManager subCrawlerManager = null;
		
	/**
	 * A class to detect charsets
	 */
	ICharsetDetector charsetDetector = null;	
	
	public CrawlerWorker(SubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
	}
	
	/**
	 * Init the parser context
	 */
	protected void initCrawlerContext() {
		// init the parser context object
		CrawlerContext parserContext = new CrawlerContext(this.charsetDetector);
		CrawlerContext.setCurrentContext(parserContext);		
	}	

	@Override
	protected void execute(ICommand command) {
		/* The Result-type 'Failure' should not be possible as only Filters are being processed before
		 * which may not set the result-status to 'Failure', only to 'Rejected' */
		if (command.getResult() == ICommand.Result.Rejected) {
			this.logger.warn("Won't fetch document '" + command.getLocation() + "' with status 'Rejected' (" + command.getResultText() + ")");
			return;
		}
		
		// init the parser context
		this.initCrawlerContext();		
		
		try {
			// get the URL to crawl
			String urlString = command.getLocation();
			URL url = new URL(urlString);

			// get a sub-crawler that is capable to handle the specified protocol
			String protocol = url.getProtocol();
			this.logger.info("Fetching crawler for protocol: " + protocol);
			
			ISubCrawler crawler = this.subCrawlerManager.getSubCrawler(protocol);
			if (crawler == null) {
				this.logger.warn("No crawler for protocol '" + protocol + "' found.");
				command.setResult(ICommand.Result.Failure, "No crawler for protocol '" + protocol + "' found.");
				return;
			}			
			
			// pass the URL to the cralwer
			ICrawlerDocument crawlerDoc = crawler.request(urlString);
			if (crawlerDoc == null) {
				command.setResult(ICommand.Result.Failure, "Unexpected error while crawling the resource");
			} else if (crawlerDoc.getStatus() != ICrawlerDocument.Status.OK) {
				command.setResult(ICommand.Result.Failure, crawlerDoc.getStatusText());
			}
			
			// TODO: mimetype detection
			// resource.getMimeType()
			command.setCrawlerDocument(crawlerDoc);
			
		} catch (Exception e) {
			command.setResult(ICommand.Result.Failure, "Unexpected error while crawling the resource");
		}
	}
	
	@Override
	protected void reset() {
		// do some cleanup
		CrawlerContext crawlerContext = CrawlerContext.getCurrentContext();
		if (crawlerContext != null) crawlerContext.reset();
		
		// reset all from parent
		super.reset();
	}
}
