/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.crawler.impl;

import java.net.URI;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.queue.ICommand;
import org.paxle.core.threading.AWorker;
import org.paxle.crawler.CrawlerContext;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

public class CrawlerWorker extends AWorker<ICommand> {
	
	protected Log logger = LogFactory.getLog(CrawlerWorker.class);
	
	/**
	 * A class to manage {@link ISubCrawler sub-crawlers}
	 */
	private final ISubCrawlerManager subCrawlerManager;
		
	public CrawlerWorker(ISubCrawlerManager subCrawlerManager) {
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
						"Won't crawl resource '%s'. Command status is: '%s' (%s)",
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
			final URI location = command.getLocation();
			
			// determine protocol to use
			String protocol = location.getScheme();

			// get a sub-crawler that is capable to handle the specified protocol
			this.logger.debug(String.format("Getting crawler for protocol '%s' ...", protocol));			
			ISubCrawler crawler = this.subCrawlerManager.getSubCrawler(protocol);
						
			if (crawler == null) {
				this.logger.error(String.format("No crawler for resource '%s' and protocol '%s' found.", location, protocol));
				command.setResult(
						ICommand.Result.Failure, 
						String.format("No crawler for protocol '%s' found.", protocol)
				);
				return;
			}
			this.logger.debug(String.format("Crawler '%s' found for protocol '%s'.", crawler.getClass().getName(), protocol));
			
			// pass the URL to the crawler
			this.logger.info(String.format("Crawling resource '%s' using protocol '%s' ...", location, protocol));
			crawlerDoc = crawler.request(location);
			
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
				crawlerDoc.setLocation(location);
			}
			if (crawlerDoc.getCrawlerDate() == null) {
				crawlerDoc.setCrawlerDate(new Date());
			}
			
			// setting command status to passed
			command.setResult(ICommand.Result.Passed, null);
			
		} catch (Throwable e) {
			// setting command status
			command.setResult(
					ICommand.Result.Failure, 
					String.format("Unexpected '%s' while crawling resource. %s",e.getClass().getName(),e.getMessage())
			);
			
			// log error
			this.logger.warn(String.format(
					"Unexpected '%s' while crawling resource '%s'.",
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
					"Finished crawling of resource '%s' (%d kb) in %d ms.\r\n" +
					"\tCrawler-Status: '%s' %s",
					command.getLocation(),
					Long.valueOf((crawlerDoc == null) ? -1L : crawlerDoc.getSize() >> 10),
					Long.valueOf(System.currentTimeMillis() - start),
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
