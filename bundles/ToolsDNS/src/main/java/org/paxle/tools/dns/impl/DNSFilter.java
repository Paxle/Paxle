package org.paxle.tools.dns.impl;

import java.net.URI;
import java.net.UnknownHostException;
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
import org.xbill.DNS.Address;

public class DNSFilter implements IFilter<ICommand> {

	private Log logger = LogFactory.getLog(this.getClass());
	
	public void filter(ICommand command, IFilterContext filterContext) {
		String uri = command.getLocation().getHost();
		try {
			Address.getByName(uri);
		} catch (UnknownHostException e) {
			command.setResult(ICommand.Result.Rejected, "unable to resolve hostname.");
			logger.info("unable to resolve hostname " + command.getLocation() + ".");
			e.printStackTrace();
		}
		
		IParserDocument parserDoc = command.getParserDocument();
		this.checkHostname(parserDoc);
	}

	private void checkHostname(IParserDocument parserDoc) {
		if (parserDoc == null) return;

		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.checkHostname(linkMap);
		}

		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkHostname(subDoc);
			}
		}
	}   

	void checkHostname(Map<URI, LinkInfo> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;

		Iterator<Entry<URI, LinkInfo>> refs = linkMap.entrySet().iterator();
		while (refs.hasNext()) {
			Entry<URI,LinkInfo> next = refs.next();
			URI location = next.getKey();
			LinkInfo meta = next.getValue();

			// skip URI that are already marked as not OK
			if (!meta.hasStatus(Status.OK)) continue;
			
			try {
				Address.getByName(location.getHost());
			} catch (UnknownHostException e) {
				meta.setStatus(Status.FILTERED, "unable to resolve hostname.");
				logger.info("unable to resolve hostname " + location + ".");
			}

		}
	}
}
