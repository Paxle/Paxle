package org.paxle.crawler.urlRedirector.impl;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.osgi.service.component.ComponentContext;
import org.paxle.core.norm.IReferenceNormalizer;
import org.paxle.data.db.ICommandDB;
import org.xsocket.connection.IServer;
import org.xsocket.connection.Server;

@Component(metatype=false, immediate=true)
public class UrlRedirectorServer {
	private IServer srv = null;
	
	@Reference
	private ICommandDB commandDB;
	
	@Reference
	private IReferenceNormalizer refNormalizer;
	
	protected void activate(ComponentContext context) throws UnknownHostException, IOException {
		// create server
		this.srv = new Server(8090, new UrlHandler(this.commandDB, this.refNormalizer));
		
		// start it
		this.srv.start();
	}
	
	protected void deactivate(ComponentContext context ) throws IOException {
		// shutdown the server
		this.srv.close();
		this.srv = null;
	}
}
