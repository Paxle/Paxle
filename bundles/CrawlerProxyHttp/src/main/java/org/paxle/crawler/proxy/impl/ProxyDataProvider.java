/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.crawler.proxy.impl;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.doc.Command;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.ICommandProfile;
import org.paxle.core.doc.ICommandTracker;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IDocumentFactory;
import org.paxle.core.prefs.Properties;
import org.xsocket.connection.http.HttpResponseHeader;

public class ProxyDataProvider extends Thread implements IDataProvider<ICommand> {
	/* =========================================================
	 * Preferences
	 * ========================================================= */
	public static final String PREF_PROFILE_ID = "profileID";	
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
		
	/**
	 * A {@link IDataSink data-sink} to write the generated {@link ICommand commands} out
	 */
	private IDataSink<ICommand> sink = null;	
	
	/**
	 * Thread-pool for {@link ProxyDataProviderCallable}
	 */
	private ExecutorService execService;	

	/**
	 * Used in {@link #run()} to fetch the {@link ICrawlerDocument}s generated
	 * by the {@link ProxyDataProviderCallable worker-threads}.
	 */
	private final CompletionService<ICrawlerDocument> execCompletionService;
	
	/**
	 * Used to notify event-listeners about the creation of a new {@link ICommand}
	 * @see ICommandTracker#commandCreated(String, ICommand)
	 */
	private final ICommandTracker commandTracker;
	
	private final IDocumentFactory docFactory;
	
	/**
	 * indicates if this thread was terminated
	 * @see #terminate()
	 */
	private boolean stopped = false;
		
	private static ProxyDataProvider singleton;
	
	/**
	 * The properties of this component
	 */
	private Properties props = null;	

	/**
	 * The {@link ICommandProfile#getOID() profile-id} that should be
	 * set on the newly created {@link ICommand}
	 * 
	 * @see ICommand#setProfileOID(int)
	 * @see #getProfileID()
	 */
	private int commandProfileID = -1;	
	
	public ProxyDataProvider(Properties props, ICommandTracker cmdTracker, IDocumentFactory docFactory) {
		singleton = this;
		this.commandTracker = cmdTracker;
		this.docFactory = docFactory;
		
		// init threadpool
		// XXX should we set the thread-pool size? 
		this.execService = Executors.newCachedThreadPool();				
		this.execCompletionService = new ExecutorCompletionService<ICrawlerDocument>(this.execService);
		
		// read preferences
		if (props != null) {
			this.props = props;
			this.commandProfileID = Integer.parseInt(props.getProperty(PREF_PROFILE_ID,"-1"));
		}
		
		// starting up the thread
		this.setName(this.getClass().getSimpleName());
		this.start();
	}
	
	/**
	 * Function to terminate this thread and to shutdown the worker-thread-pool. 
	 * @throws InterruptedException
	 */
	public void terminate() throws InterruptedException {
		this.stopped = true;
		this.interrupt();
		
		// shutdown exec-service
		// XXX maybe we should use shutdownNow here?
		this.execService.shutdown();
		
		// wait to finish termination
		this.join(5000);
	}
	
	public static void process(URI location, HttpResponseHeader resHdr, InputStream bodyInputStream) {
		singleton.processNext(location, resHdr, bodyInputStream);
	}
	
	private void processNext(URI location, HttpResponseHeader resHdr, InputStream bodyInputStream) {
		// TODO: check if we are overloaded
		
		if (this.logger.isDebugEnabled()) this.logger.debug(String.format("Starting a new worker for '%s'.", location));
		this.execCompletionService.submit(new ProxyDataProviderCallable(location, resHdr, bodyInputStream, this.docFactory));
	}
	
	@Override
	public void run() {
		try {			
			// waiting until the data-sink was set from outside
			synchronized (this) {
				while (this.sink == null) this.wait();
			}
			
			// waiting for new commands
			while(!this.stopped && !Thread.interrupted()) {
				try {
					// fetch the next complete crawler document
					ICrawlerDocument crawlerDoc = execCompletionService.take().get();
					if (crawlerDoc != null && crawlerDoc.getStatus() == ICrawlerDocument.Status.OK) {

						// create a new ICommand
						ICommand cmd = Command.createCommand(crawlerDoc.getLocation(), this.getProfileID(), 0);
						cmd.setResult(ICommand.Result.Passed, null);
						
						/* Sending event via command-tracker!
						 * 
						 * Calling this function should also created a valid command OID for us
						 */ 
						this.commandTracker.commandCreated(this.getClass().getName(), cmd);
						if (cmd.getOID() <= 0) {
							this.logger.warn(String.format(
									"Command with location '%s' has an invalid OID '%d'. ORM mapping seems not to work. Command is not enqueued.",									
									cmd.getLocation(),
									Integer.valueOf(cmd.getOID())
							));
						} else {
							cmd.setCrawlerDocument(crawlerDoc);
							
							// put it into the data-sink
							this.sink.putData(cmd);
						}
					}
				} catch (Exception e) {
					if (!(e instanceof InterruptedException)) {
						this.logger.error(String.format(
								"%s: Unexpected '%s' while waiting for new commands to enqueue.",
								this.getName(),
								e.getClass().getName()
						),e);
					} else {
						this.logger.info("Thread stopped successfully.");
						break;
					}
				} 
			}			
		} catch (Exception e) {
			this.logger.error(String.format(
					"%s: Unexpected '%s'.",
					this.getName(),
					e.getClass().getName()
			),e);
		} 
	}
	
	private int getProfileID() {		
		if (this.commandProfileID == -1) {
			// TODO: create a new command-profile
		}		
		return this.commandProfileID;
	}

	/**
	 * @see IDataProvider#setDataSink(IDataSink)
	 */
	public synchronized void setDataSink(IDataSink<ICommand> dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null.");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		
		this.sink = dataSink;
		this.notify();	
	}
}
