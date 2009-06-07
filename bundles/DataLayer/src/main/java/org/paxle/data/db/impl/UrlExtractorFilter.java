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
package org.paxle.data.db.impl;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Constants;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.StatusVariable;
import org.paxle.core.data.IDataProvider;
import org.paxle.core.data.IDataSink;
import org.paxle.core.doc.ICommand;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.LinkInfo;
import org.paxle.core.doc.LinkInfo.Status;
import org.paxle.core.filter.IFilter;
import org.paxle.core.filter.IFilterContext;
import org.paxle.data.db.URIQueueEntry;

public class UrlExtractorFilter implements IFilter<ICommand>, IDataProvider<URIQueueEntry>, Monitorable {
	/**
	 * {@link Constants#SERVICE_PID} used to register the {@link Monitorable} interface
	 */
	public static final String PID = "org.paxle.data.UrlExtractor";

	private static final String VAR_NAME_QUEUE_SIZE = "queue.size";	
	
	/**
	 * The names of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private static final HashSet<String> VAR_NAMES =  new HashSet<String>(Arrays.asList(new String[]{
			VAR_NAME_QUEUE_SIZE
	}));
	
	/**
	 * Descriptions of all {@link StatusVariable status-variables} supported by this {@link Monitorable}
	 */
	private final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/UrlExtractorFilter");
	
	/**
	 * Class to count processed {@link URI}
	 */
	private static class Counter {
		/**
		 * Total number of checked {@link URI}.
		 */
		public int total = 0;
		
		/**
		 * Number of {@link URI} selected for storage to DB.
		 */
		public int enqueued = 0;
	}
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * A queue to buffer all {@link URI} that were recently extracted
	 * from an {@link IParserDocument} and are enqueued for insertion
	 * into the {@link #db command-db} 
	 */
	private final BlockingQueue<URIQueueEntry> extractedUriQueue;

	/**
	 * A {@link Thread} used to listen for newly {@link #extractedUriQueue extracted-URI}
	 * that should be stored into the {@link #db command-db}
	 */
	private final URIStorageThread storageThread;
	
	private IDataSink<URIQueueEntry> sink = null;
	
	public UrlExtractorFilter() {
		// create the URI queue 
		this.extractedUriQueue = new LinkedBlockingQueue<URIQueueEntry>();
		
		// create and start the worker thread
		this.storageThread = new URIStorageThread();
		this.storageThread.start();
	}
	
	/**
	 * @see IDataProvider#setDataSink(IDataSink)
	 */
	public synchronized void setDataSink(IDataSink<URIQueueEntry> dataSink) {
		if (dataSink == null) throw new NullPointerException("The data-sink is null.");
		if (this.sink != null) throw new IllegalStateException("The data-sink was already set.");
		this.sink = dataSink;
		this.notify();
	}
	
	/**
	 * @see IFilter#filter(Object, IFilterContext)
	 */
	public void filter(ICommand command, IFilterContext context) {
		if (command == null) return;
		
		// getting the parser-doc
		IParserDocument parserDoc = command.getParserDocument();
		if (parserDoc == null) return;
		
		// getting the link map
		final Counter c = new Counter();
		this.extractLinks(command, null, parserDoc, c);
		logger.info(String.format(
				"Selected %d URI out of %d URI from '%s' for storage to DB.",
				Integer.valueOf(c.enqueued), 
				Integer.valueOf(c.total), 
				command.getLocation()
		));
	}
	
	private void extractLinks(final ICommand command, String internalName, IParserDocument parserDoc, final Counter c) {
		if (parserDoc == null) return;
		
		// getting the link map
		Map<URI, LinkInfo> linkMap = parserDoc.getLinks();
		if (linkMap != null) {
			c.total += linkMap.size();
			
			if ((parserDoc.getFlags() & IParserDocument.FLAG_NOFOLLOW) == 0) {
				this.extractLinks(command, linkMap, c);
			} else {
				logger.info(String.format("Omitting link-extraction from '%s' due to 'nofollow'-flag",
						(internalName == null) ? command.getLocation() : internalName));
			}
		}
		
		Map<String,IParserDocument> subDocs = parserDoc.getSubDocs();
		if (subDocs != null) {
			for (Entry<String,IParserDocument> subDocEntry : subDocs.entrySet()) {
				this.extractLinks(command, subDocEntry.getKey(), subDocEntry.getValue(), c);
			}
		}
	}
	
	private void extractLinks(final ICommand command, Map<URI, LinkInfo> linkMap, final Counter c) {
		if (linkMap == null) return;
		
		final LinkedList<URI> refs = new LinkedList<URI>();		
		for (Entry<URI, LinkInfo> link : linkMap.entrySet()) {
			URI ref = link.getKey();
			LinkInfo meta = link.getValue();
			
			// check if the URI exceeds max length
			if (ref.toString().length() > 512) {
				this.logger.debug("Skipping too long URL: " + ref);
				continue;
			} else if (!meta.hasStatus(Status.OK)) {
				this.logger.debug(String.format(
						"Skipping URL because of status '%s' (%s): %s",
						meta.getStatus(),
						meta.getStatusText(),
						ref
				));
				continue;
			}
			
			c.enqueued++;
			refs.add(ref);
		}
		
		if (refs.size() > 0) {
			// add command into URI queue
			this.extractedUriQueue.add(new URIQueueEntry(
					command.getLocation(),
					command.getProfileOID(),
					command.getDepth() + 1,
					refs
			));
		}
	}
	
	public void terminate() throws InterruptedException {
		if (this.storageThread != null) {
			// interrupt thread
			this.storageThread.interrupt();
			
			// wait for shutdown
			this.storageThread.join(1000);
		}
		
		// clear URI queue
		this.extractedUriQueue.clear();
	}

	/**
	 * A thread used to store {@link URI} async. to {@link CommandDB}.
	 */
	private class URIStorageThread extends Thread {
		public URIStorageThread() {
			this.setName(this.getClass().getSimpleName());
		}
		
		@Override
		public void run() {
			try {
				
				synchronized (UrlExtractorFilter.this) {
					while (sink == null) UrlExtractorFilter.this.wait();
				}
				
				while (!this.isInterrupted()) {
					try {
						
						// waiting for the next job
						URIQueueEntry entry = extractedUriQueue.take();
						
						// store unknown URIs						
						// the list is being modified by CommandDB#storeUnknownLocations, so we need to save the size first
						final int totalLocations = entry.getReferences().size();
						sink.putData(entry);
						
						logger.info(String.format(
								"Extracted %d new and %d already known URIs from '%s'",
								Integer.valueOf(totalLocations - entry.getKnown()), 
								Integer.valueOf(entry.getKnown()), 
								entry.getRootURI().toASCIIString()
						));
						
					} catch (Throwable e) {
						if (e instanceof InterruptedException) throw (InterruptedException) e;
						logger.error(String.format(
								"Unexpected '%s' while trying to store new URI into the command-db.",
								e.getClass().getName()
						), e);
					}
				}
			} catch (InterruptedException e) {
				logger.info(String.format(
						"Shutdown of '%s' finished. '%d' could not be stored.",
						this.getName(),
						Integer.valueOf(extractedUriQueue.size())
				));
				return;
			}
		}
	}

	/* =========================================================================
	 * Monitorable support
	 * ========================================================================= */	
	
	public String getDescription(String id) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(id)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + id);
		}	
		
		return this.rb.getString(id);
	}

	public StatusVariable getStatusVariable(String id) throws IllegalArgumentException {
		if (!VAR_NAMES.contains(id)) {
			throw new IllegalArgumentException("Invalid Status Variable name " + id);
		}		
		
		int value = -1;
		if (id.equals(VAR_NAME_QUEUE_SIZE)) {
			value = this.extractedUriQueue.size();
		} 
		
		return new StatusVariable(id, StatusVariable.CM_GAUGE, value);
	}

	public String[] getStatusVariableNames() {
		return VAR_NAMES.toArray(new String[VAR_NAMES.size()]);
	}

	public boolean notifiesOnChange(String id) throws IllegalArgumentException {
		return false;
	}

	public boolean resetStatusVariable(String id) throws IllegalArgumentException {
		return false;
	}
}
