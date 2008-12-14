/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

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
		try {
			// getting the host name
			String hostName = command.getLocation().getHost();
			if (hostName == null || hostName.length() == 0) {
				command.setResult(ICommand.Result.Rejected, "Invalid hostname.");
				logger.info(String.format(
						"Invalid hostname detected for command with location '%s'.",
						command.getLocation()
				));
				return;
			}
			
			// trying to do a dns lookup
			Address.getByName(hostName);
		} catch (UnknownHostException e) {
			command.setResult(ICommand.Result.Rejected, "Unable to resolve hostname.");
			logger.info(String.format(
					"Unable to resolve hostname for command '%s'.",
					command.getLocation()
			));
		}
		
		IParserDocument parserDoc = command.getParserDocument();
		this.checkHostname(command, parserDoc);
	}

	private void checkHostname(ICommand cmd, IParserDocument parserDoc) {
		if (parserDoc == null) return;

		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			this.checkHostname(cmd, linkMap);
		}

		// loop through sub-parser-docs
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (IParserDocument subDoc : subDocs.values()) {
				this.checkHostname(cmd, subDoc);
			}
		}
	}   

	void checkHostname(ICommand cmd, Map<URI, LinkInfo> linkMap) {
		if (linkMap == null || linkMap.size() == 0) return;

		Iterator<Entry<URI, LinkInfo>> refs = linkMap.entrySet().iterator();
		while (refs.hasNext()) {
			Entry<URI,LinkInfo> next = refs.next();
			URI location = next.getKey();
			LinkInfo meta = next.getValue();

			// skip URI that are already marked as not OK
			if (!meta.hasStatus(Status.OK)) continue;
			
			String hostName = null;
			try {
				hostName = location.getHost();
				if (hostName == null || hostName.length() == 0) {
					meta.setStatus(Status.FILTERED, "Invalid hostname.");
					this.logger.info(String.format(
							"Invalid hostname detected while filtering URLs from reference map(s) of command '%s'.",
							cmd.getLocation()
					));
					continue;
				}
				
				Address.getByName(location.getHost());
			} catch (UnknownHostException e) {
				meta.setStatus(Status.FILTERED, "Unable to resolve hostname.");
				this.logger.info(String.format(
						"Unable to resolve hostname '%s' while filtering URLs from reference map(s) of command '%s'.",
						hostName,
						cmd.getLocation()
				));
			}

		}
	}
}
