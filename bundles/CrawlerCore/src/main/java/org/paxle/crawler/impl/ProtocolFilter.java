package org.paxle.crawler.impl;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.paxle.crawler.ISubCrawler;
import org.paxle.crawler.ISubCrawlerManager;

/**
 * Filters {@link ICommand commands} out if the protocol of the
 * resource is not supported by one of the available {@link ISubCrawler sub-crawlers}
 */
public class ProtocolFilter implements IFilter<ICommand> {

	/* ==============================================================
	 * CONSTANTS for error-messages
	 * ============================================================== */
	private static final String ERR_NOTSUPPORTED = "Protocol '%s' not supported";
	private static final String ERR_NOPROT = "Malformed URL. No protocol was specified";
	
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Component needed to determine if a given protocol is supported by one
	 * of the installed {@link ISubCrawler}s.
	 * 
	 * @see ISubCrawlerManager#isSupported(String)
	 */
	private ISubCrawlerManager subCrawlerManager = null;

	public ProtocolFilter(ISubCrawlerManager subCrawlerManager) {
		this.subCrawlerManager = subCrawlerManager;
	}

	/**
	 * @see IFilter#filter(ICommand)
	 */
	public void filter(ICommand command, IFilterContext context) {
		try {
			// check the command location
			final URI location = command.getLocation();
			final String scheme = location.getScheme();
			
			if (scheme.length() == 0) {
				command.setResult(ICommand.Result.Rejected, ERR_NOPROT);
			} else if (!this.subCrawlerManager.isSupported(scheme)) {
				command.setResult(ICommand.Result.Rejected, String.format(ERR_NOTSUPPORTED, scheme));
				return;
			}
			
			// check the extracted links
			IParserDocument parserDoc = command.getParserDocument();
			this.checkProtocol(parserDoc);
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected %s while filtering command with location '%s'.",
					e.getClass().getName(),
					command.getLocation()),
					e
			);
		}
	}

	void checkProtocol(IParserDocument parserDoc) {
		if (parserDoc == null) return;
		
		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
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
	
	void checkProtocol(Map<URI, LinkInfo> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;
		
		Iterator<Entry<URI,LinkInfo>> refs = linkMap.entrySet().iterator();
		while (refs.hasNext()) {
			Entry<URI,LinkInfo> refEntry = refs.next();			
			URI uri = refEntry.getKey();
			LinkInfo uriMetadata = refEntry.getValue();
			
			// skip URI that are already marked as not OK
			if (!uriMetadata.hasStatus(Status.OK)) continue;

			// check the URI
			try {
				this.checkProtocol(uri);
			} catch (ProtocolFilterException pe) {
				uriMetadata.setStatus(Status.FILTERED, pe.getMessage());
				this.logger.info(String.format("URL '%s' blocked: %s", uri, pe.getMessage()));
			}	
		}		
	}
	
	void checkProtocol(URI location) throws ProtocolFilterException {
		String protocol = location.getScheme();
		if (protocol == null || protocol.length() == 0)
			throw new ProtocolFilterException(ERR_NOPROT);

		// check if the protocol is supported by one of the 
		// available sub-crawlers
		if (!this.subCrawlerManager.isSupported(protocol)) {
			throw new ProtocolFilterException(String.format(ERR_NOTSUPPORTED, protocol));
		}
	}
}
