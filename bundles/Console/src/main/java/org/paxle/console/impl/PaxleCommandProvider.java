package org.paxle.console.impl;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.paxle.core.IMWComponent;

public class PaxleCommandProvider implements CommandProvider {

	private static enum COMPONENT {
		Crawler("org.paxle.crawler"),
		Parser("org.paxle.parser"),
		Indexer("org.paxle.indexer");
		
		public String id;
		private COMPONENT(String id) {
			this.id = id;
		};
	}
	
	private final BundleContext context;
	
	public PaxleCommandProvider(BundleContext context) {
		this.context = context;
	}

	private IMWComponent getMWComponent(COMPONENT component) throws InvalidSyntaxException {
		ServiceReference[] crawlerRef = this.context.getServiceReferences(
				IMWComponent.class.getName(), 
				String.format("(%s=%s)", IMWComponent.COMPONENT_ID,component.id)
		);
		if (crawlerRef == null) return null;
		else if (crawlerRef.length > 1) {
			throw new IllegalStateException(String.format("More than one '%s' component found.", component.toString()));
		}
				
		return (IMWComponent) this.context.getService(crawlerRef[0]);
	}
	
	public void _crawler(CommandInterpreter ci) throws InvalidSyntaxException {		
		this.mwComponentCommands(COMPONENT.Crawler, ci, ci.nextArgument());
	}
		
	public void _parser(CommandInterpreter ci) throws InvalidSyntaxException {		
		this.mwComponentCommands(COMPONENT.Parser, ci, ci.nextArgument());
	}	
	
	public void _indexer(CommandInterpreter ci) throws InvalidSyntaxException {		
		this.mwComponentCommands(COMPONENT.Indexer, ci, ci.nextArgument());
	}
	
	public void _components(CommandInterpreter ci) throws InvalidSyntaxException {		
		this.mwComponentCommands(COMPONENT.values(), ci, ci.nextArgument());
	}
	
	private void mwComponentCommands(COMPONENT componentName, CommandInterpreter ci, String arg) throws InvalidSyntaxException {
		this.mwComponentCommands(new COMPONENT[]{componentName}, ci, arg);
	}
	
	private void mwComponentCommands(COMPONENT[] componentNames, CommandInterpreter ci, String arg) throws InvalidSyntaxException {
		if (arg == null) {
			ci.println("No argument found");
			return;
		}
		
		for (COMPONENT componentName : componentNames) {
			IMWComponent<?> component = this.getMWComponent(componentName);
			if (component == null) {
				ci.println(String.format("No %s found", componentName.name()));
				return;
			}

			if (arg.equals("status")) {
				ci.println(String.format("%s status:", componentName.name()));
				ci.println(String.format(" Activity = %s",(component.isPaused())?"paused":"running"));
				ci.println(String.format(" PPM = %d",component.getPPM()));
				ci.println(String.format(" Active Jobs = %d",component.getActiveJobCount()));
				ci.println(String.format(" Enqueued Jobs = %d",component.getEnqueuedJobCount()));
				ci.println();
			} else if (arg.equals("pause")) {
				component.pause();
				ci.println(String.format("%s paused.", componentName.name()));
			} else if (arg.equals("resume")) {
				component.resume();
				ci.println(String.format("%s resumed.", componentName.name()));
			}
		}
	}
	
	public String getHelp() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("Paxle commands:\r\n")
		   .append("---Controlling the Crawler/Parser/Indexer---\r\n")
		   .append("\tcrawler|parser|indexer|components - Paxle component related commands\r\n")
		   .append("\t   status - print current status\r\n")
		   .append("\t   pause - pause component\r\n")
		   .append("\t   resume - resume component\r\n");
		
		return buf.toString();
	}

}
