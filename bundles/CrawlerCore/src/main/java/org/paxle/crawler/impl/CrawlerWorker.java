package org.paxle.crawler.impl;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
		
	public CrawlerWorker(SubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
	}

	@Override
	protected void execute(ICommand command) {
		final long start = System.currentTimeMillis();
		ICrawlerDocument crawlerDoc = null;
		try {
			/* ================================================================
			 * Input Parameter Check
			 * ================================================================ */
			String errorMsg = null;
			if (command.getResult() != ICommand.Result.Passed) {
				errorMsg = String.format(
						"Won't parse resource '%s'. Command status is: '%s' (%s)",
						command.getLocation(),
						command.getResult(),
						command.getResultText()
				);
			}
			
			if (errorMsg != null) {
				this.logger.warn(errorMsg);
				return;
			}

			/* ================================================================
			 * Crawl Resource
			 * 
			 * a) determine resource protocol
			 * b) fetch appropriate crawler
			 * c) crawl resource
			 * d) process crawler response
			 * ================================================================ */
			
			// get the URL to crawl
			String urlString = command.getLocation();
			
			// determine protocol to use
			int idx = urlString.indexOf("://");
			String protocol = (idx == -1) ? "" : urlString.substring(0,idx);

			// get a sub-crawler that is capable to handle the specified protocol
			this.logger.debug(String.format("Getting crawler for protocol '%s' ...", protocol));			
			ISubCrawler crawler = this.subCrawlerManager.getSubCrawler(protocol);
			this.logger.debug(String.format("Crawler '%s' found for protocol '%s'.", crawler.getClass().getName(), protocol));
						
			if (crawler == null) {
				this.logger.error(String.format("No crawler for resource '%s' and protocol '%s' found.",urlString, protocol));
				command.setResult(
						ICommand.Result.Failure, 
						String.format("No crawler for protocol '%s' found.", protocol)
				);
				return;
			}			
			
			// pass the URL to the crawler
			this.logger.info(String.format("Crawling resource '%s' using protocol '%s' ...", urlString, protocol));
			crawlerDoc = crawler.request(urlString);
			
			/* ================================================================
			 * Process crawler response
			 * ================================================================ */
			
			if (crawlerDoc == null) {
				command.setResult(
						ICommand.Result.Failure, 
						String.format("Crawler '%s' returned no crawler-document.",crawler.getClass().getName())
				);
				return;
			} else if (crawlerDoc.getStatus() == null || crawlerDoc.getStatus() != ICrawlerDocument.Status.OK) {
				command.setResult(
						ICommand.Result.Failure, 
						String.format("Crawler-document status is '%s'.",crawlerDoc.getStatus())
				);
				return;
			}
			
			// handling of default properties
			if (crawlerDoc.getLocation() == null) {
				crawlerDoc.setLocation(urlString);
			}
			if (crawlerDoc.getCrawlerDate() == null) {
				crawlerDoc.setCrawlerDate(new Date());
			}
			
			// setting command status to passed
			command.setResult(ICommand.Result.Passed, null);
			
		} catch (Exception e) {
			// setting command status
			command.setResult(
					ICommand.Result.Failure, 
					String.format("Unexpected '%s' while crawling resource. %s",e.getClass().getName(),e.getMessage())
			);
			
			// log error
			this.logger.warn(String.format("Unexpected '%s' while crawling resource '%s'.",
					e.getClass().getName(),
					command.getLocation()
			),e);
		} finally {
			/*
			 * Append crawler-doc to command object
			 * 
			 * This must be done even in error situations to 
			 * - allow filters to correct the error (if possible)
			 * - to report the error back properly (e.g. to store it into db
			 *   or send it back to a remote peer).  
			 */
			if (crawlerDoc != null) {
				command.setCrawlerDocument(crawlerDoc);
			}
			
			this.logger.info(String.format(
					"Finished crawling of resource '%s' in %d ms.\r\n" +
					"\tCrawler-Status: '%s' %s",
					command.getLocation(),
					System.currentTimeMillis() - start,
					(crawlerDoc == null) ? "unknown" : crawlerDoc.getStatus().toString(),
					(crawlerDoc == null) ? "" : (crawlerDoc.getStatusText()==null)?"":crawlerDoc.getStatusText()
			));
		}
	}
	
	@Override
	protected void reset() {
		// do some cleanup
		CrawlerContext.removeCurrentContext();
		
		// reset all from parent
		super.reset();
	}
}