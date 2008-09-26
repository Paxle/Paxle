package org.paxle.tools.dns.impl;

import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.core.queue.ICommand;
import org.xbill.DNS.Address;

public class DNSFilter implements IFilter<ICommand> {

	private Log logger = LogFactory.getLog(this.getClass());
	
	public void filter(ICommand command, IFilterContext filterContext) {
		// TODO Auto-generated method stub
		String uri = command.getLocation().getHost();
		try {
			Address.getByName(uri);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			command.setResult(ICommand.Result.Rejected, "unable to resolve hostname.");
			logger.info("unable to resolve hostname " + command.getLocation() + ".");
			e.printStackTrace();
		}
	}

}
