package org.paxle.filter.robots.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class RobotsTxtFilter implements IFilter<ICommand> {
	
	/**
	 * Class to count rejected URI
	 */
	private static class Counter {		
		public int c = 0;
	}
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	private RobotsTxtManager robotsTxtManager = null;
	
	public RobotsTxtFilter(RobotsTxtManager robotsTxtManager) {
		this.robotsTxtManager = robotsTxtManager;
	}
	
	/**
	 * @see IFilter#filter(ICommand)
	 */
	public void filter(ICommand command, IFilterContext filterContext) {		
		// getting the location
		URI location = command.getLocation();

		try {

			// test if the url is disallowed by robots.txt
			if (this.robotsTxtManager.isDisallowed(location)) {
				command.setResult(ICommand.Result.Rejected, "Access disallowed by robots.txt");
				return;
			}

			// check the extracted links
			final Counter c = new Counter();
			IParserDocument parserDoc = command.getParserDocument();
			this.checkRobotsTxt(parserDoc, c);
			logger.info(String.format("removed %d URLs from reference map(s) of '%s'", Integer.valueOf(c.c), command.getLocation())); 
		} catch (Exception e) {
			this.logger.error(String.format("Unexpected %s while filtering command with location '%s'.",e.getClass().getName(),location),e);
		}
	}
	
	private void checkRobotsTxt(IParserDocument parserDoc, final Counter c) {
		if (parserDoc == null) return;
		
		// getting the link map
		Map<URI, String> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.checkRobotsTxt(linkMap, c);
		}
		
		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkRobotsTxt(subDoc, c);
			}
		}
	}	
	
	private void checkRobotsTxt(Map<URI, String> linkMap, final Counter c) {
		if (linkMap == null || linkMap.size() == 0) return;
		
		Collection<URI> disallowedURI = this.robotsTxtManager.isDisallowed(linkMap.keySet());
		if (disallowedURI != null && disallowedURI.size() > 0) {
			for (URI location : disallowedURI) {
				// TODO: logging
				linkMap.remove(location);
			}
		}
//		Iterator<URI> refs = linkMap.keySet().iterator();
//		while (refs.hasNext()) {
//			String location = refs.next().toString();		// XXX: should this be .toASCIIString()?
//
//			if (this.robotsTxtManager.isDisallowed(location)) {
//				refs.remove();
//				c.c++;
//				if (logger.isDebugEnabled())
//					this.logger.debug(String.format("URL '%s' removed from reference map.", location));
//			}
//		}		
	}
}
