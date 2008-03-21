package org.paxle.filter.robots.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class RobotsTxtFilter implements IFilter<ICommand> {
	
	private static class Counter {
		
		public int c = 0;
	}
	
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
		String location = command.getLocation();

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
		Map<String, String> linkMap = parserDoc.getLinks();
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
	
	private void checkRobotsTxt(Map<String, String> linkMap, final Counter c) {
		if (linkMap == null || linkMap.size() == 0) return;
		
		Iterator<String> refs = linkMap.keySet().iterator();
		while (refs.hasNext()) {
			String location = refs.next();

			if (this.robotsTxtManager.isDisallowed(location)) {
				refs.remove();
				c.c++;
				if (logger.isDebugEnabled())
					this.logger.debug(String.format("URL '%s' removed from reference map.", location));
			}
		}		
	}
}
