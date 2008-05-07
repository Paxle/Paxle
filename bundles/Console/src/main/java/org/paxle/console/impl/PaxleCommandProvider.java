package org.paxle.console.impl;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.paxle.core.IMWComponent;

public class PaxleCommandProvider implements CommandProvider {
	
	private final BundleContext context;
	
	public PaxleCommandProvider(BundleContext context) {
		this.context = context;
	}

	private IMWComponent getCrawler() {
		// TODO: getting "org.paxle.core.IMWComponent","(component.ID=org.paxle.crawler)	
		return null;
	}
	
	public void _crawler(CommandInterpreter ci) {		
		String arg = ci.nextArgument();
		
		IMWComponent<?> crawler = this.getCrawler();
		if (arg.equals("status")) {
			
		} else if (arg.equals("pause")) {
			
		} else if (arg.equals("resume")) {
			
		}
	}
	
	public String getHelp() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("Paxle commands:\r\n")
		   .append("\tcrawler - Paxle Crawler related commands\r\n")
		   .append("\t\tstatus - print current crawler status")
		   .append("\t\tpause - pause crawling\r\n")
		   .append("\t\tresume - resume crawling\r\n");
		
		return buf.toString();
	}

}
