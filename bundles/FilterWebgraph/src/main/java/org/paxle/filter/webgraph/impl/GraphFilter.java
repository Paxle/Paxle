

package org.paxle.filter.webgraph.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.filter.FilterQueuePosition;
import org.paxle.core.filter.FilterTarget;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;

/**
 * This helper class determines the language of a document and inserts its finding into a parser-doc and all of its subdocs 
 * 
 * @scr.component name="org.paxle.filter.webgraph.impl.GraphFilter"
 * @scr.service interface="org.paxle.core.filter.IFilter"
 */
@FilterTarget(@FilterQueuePosition(
		queue = "org.paxle.parser.out", 
		position = 0,
		enabled = false
))
public class GraphFilter implements IFilter<ICommand> {
	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(this.getClass());
	private LRUMap domainRelations=new LRUMap(5000);
	
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) throw new NullPointerException("The command object is null.");
		try {
			if (command.getResult() != ICommand.Result.Passed) return;
			String domain1=command.getLocation().getHost();
			if(domain1.startsWith("www."))
				domain1=domain1.substring(4);
			Map<URI, LinkInfo> links=command.getParserDocument().getLinks();
			Iterator<URI> it = links.keySet().iterator();
			HashSet<String> domains=new HashSet<String>();
			String domain2;
			while(it.hasNext()){
				domain2=it.next().getHost();
				if(domain2.startsWith("www."))
					domain2=domain2.substring(4);
				domains.add(domain2);
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
	public LRUMap getRelations(){
		return domainRelations;
	}
}
