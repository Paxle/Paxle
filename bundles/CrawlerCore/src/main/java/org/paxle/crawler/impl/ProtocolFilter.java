package org.paxle.crawler.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.crawler.ISubCrawler;

/**
 * Filters {@link ICommand commands} out if the protocol of the
 * resource is not supported by one of the available {@link ISubCrawler sub-crawlers}
 */
public class ProtocolFilter implements IFilter<ICommand> {
	private Log logger = LogFactory.getLog(this.getClass());
	private SubCrawlerManager subCrawlerManager = null;

	public ProtocolFilter(SubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
	}

	/**
	 * @see IFilter#filter(ICommand)
	 */
	public void filter(ICommand command, IFilterContext context) {
		try {
			// check the command location
			String location = command.getLocation();
			try {
				this.checkProtocol(location);
			} catch (ProtocolFilterException pe) {
				command.setResult(ICommand.Result.Rejected, pe.getMessage());
				return;
			}			
			
			// check the extracted links
			IParserDocument parserDoc = command.getParserDocument();
			this.checkProtocol(parserDoc);
		} catch (Exception e) {
			this.logger.error(String.format("Unexpected %s while filtering command with location '%s'.",e.getClass().getName(),command.getLocation()),e);
		}
	}

	private void checkProtocol(IParserDocument parserDoc) {
		if (parserDoc == null) return;
		
		// getting the link map
		Map<String, String> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.checkProtocol(linkMap);
		}
		
		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkProtocol(subDoc);
			}
		}
	}	
	
	private void checkProtocol(Map<String, String> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;
		
		Iterator<String> refs = linkMap.keySet().iterator();
		while (refs.hasNext()) {
			String location = refs.next();

			try {
				this.checkProtocol(location);
			} catch (ProtocolFilterException pe) {
				refs.remove();
				this.logger.info(String.format("URL '%s' removed from reference map. %s", location, pe.getMessage()));
			}	
		}		
	}
	
	private void checkProtocol(String location) throws ProtocolFilterException {
		int idx = location.indexOf("://");
		if (idx == -1) throw new ProtocolFilterException("Malformed URL. No protocol was specified");
		String protocol = location.substring(0,idx);

		// check if the protocol is supported by one of the 
		// available sub-crawlers
		if (!this.subCrawlerManager.isSupported(protocol)) {
			throw new ProtocolFilterException(String.format("Protocol '%s' not supported",protocol));
		}
	}
}
