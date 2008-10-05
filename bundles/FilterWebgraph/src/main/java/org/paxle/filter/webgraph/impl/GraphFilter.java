

package org.paxle.filter.webgraph.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

public class GraphFilter implements IFilter<ICommand> {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	private HashMap<String, Set<String>> domainRelations=new HashMap<String, Set<String>>();
	
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");
		try {
			if (command.getResult() != ICommand.Result.Passed) return;
			String domain1=command.getLocation().getHost();
			Map<URI, LinkInfo> links=command.getParserDocument().getLinks();
			Iterator<URI> it = links.keySet().iterator();
			HashSet<String> domains=new HashSet<String>();
			while(it.hasNext()){
				domains.add(it.next().getHost());
			}
			domainRelations.put(domain1, domains);
			//TODO some filtering for importance, so the graph does not get too big.
			
			
		} catch (Exception e) {
			this.logger.error(String.format(
					"Unexpected %s while processing command with URI '%s'.",
					e.getClass().getName(),
					command.getLocation().toASCIIString()
			),e);
		}			
	}
	public Map<String, Set<String>> getRelations(){
		return domainRelations;
	}
}
