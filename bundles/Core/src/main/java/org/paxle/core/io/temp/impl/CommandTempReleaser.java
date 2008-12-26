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

package org.paxle.core.io.temp.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.core.queue.CommandEvent;
import org.paxle.core.queue.ICommand;
import org.paxle.core.queue.ICommandTracker;

public class CommandTempReleaser implements EventHandler {
	
	/**
	 * For logging
	 */
	private final Log logger = LogFactory.getLog(CommandTempReleaser.class);
		
	private final ICommandTracker tracker;
	private final ITempFileManager tfm;
	
	public CommandTempReleaser(final ITempFileManager tfm, final ICommandTracker tracker) {
		this.tfm = tfm;
		this.tracker = tracker;
	}
	
	private void releaseCommandFiles(final ICommand cmd, final Long id) {
		try {
			File file;
			final ICrawlerDocument cdoc = cmd.getCrawlerDocument();
			if (cdoc != null && (file = cdoc.getContent()) != null) {
				try {
					tfm.releaseTempFile(file);
				} catch (FileNotFoundException e) { 
					this.logger.warn("downloaded crawler-data not available for release"); 
				}
			}
			
			final Queue<Map.Entry<String,IParserDocument>> pdocs = new LinkedList<Map.Entry<String,IParserDocument>>();
			
			IParserDocument pdoc = cmd.getParserDocument();
			Map.Entry<String,IParserDocument> entry = null;
			if (pdoc != null) {
				do {
					if (entry != null) {
						pdoc = entry.getValue();
					}

					if ((file = pdoc.getTextFile()) != null) {
						try {
							tfm.releaseTempFile(file);
						} catch (FileNotFoundException e) {
							final String msg = (entry == null) ? "parser-document" : "sub parser-document '" + entry.getKey() + "'";
							logger.warn(String.format("data of %s of cmd [%06d] not available for release", msg, id));
						}
					}
					
					pdocs.addAll(pdoc.getSubDocs().entrySet());
				} while ((entry = pdocs.poll()) != null);
			}
			
		} catch (IOException e) { 
			this.logger.error("I/O error during release of temporary files", e); 
		}
	}
	
	/**
	 * @see EventHandler#handleEvent(Event)
	 */
	public void handleEvent(Event event) {
		// getting the ID of the command that was destroyed
		final Long id = (Long)event.getProperty(CommandEvent.PROP_COMMAND_ID);
		
		// getting a reference command from the command-tracker
		final ICommand cmd = this.tracker.getCommandByID(id);
		if (cmd != null) {
			// releasing temp file(s)
			this.releaseCommandFiles(cmd, id);
		}
	}
}
