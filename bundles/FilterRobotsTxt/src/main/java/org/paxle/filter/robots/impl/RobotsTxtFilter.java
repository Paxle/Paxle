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
			IParserDocument parserDoc = command.getParserDocument();
			this.checkRobotsTxt(parserDoc);
		} catch (Exception e) {
			this.logger.error(String.format("Unexpected %s while filtering command with location '%s'.",e.getClass().getName(),location),e);
		}
	}
	
	private void checkRobotsTxt(IParserDocument parserDoc) {
		if (parserDoc == null) return;
		
		// getting the link map
		Map<String, String> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.checkRobotsTxt(linkMap);
		}
		
		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkRobotsTxt(subDoc);
			}
		}
	}	
	
	private void checkRobotsTxt(Map<String, String> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;
		
		Iterator<String> refs = linkMap.keySet().iterator();
		while (refs.hasNext()) {
			String location = refs.next();

			if (this.robotsTxtManager.isDisallowed(location)) {
				refs.remove();
				this.logger.info(String.format("URL '%s' removed from reference map.", location));
			}
		}		
	}
}
