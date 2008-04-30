
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
			if (cdoc != null && (file = cdoc.getContent()) != null) try {
				tfm.releaseTempFile(file);
			} catch (FileNotFoundException e) { logger.warn("downloaded crawler-data not available for release"); }
			
			final Queue<Map.Entry<String,IParserDocument>> pdocs = new LinkedList<Map.Entry<String,IParserDocument>>();
			
			IParserDocument pdoc = cmd.getParserDocument();
			Map.Entry<String,IParserDocument> entry = null;
			if (pdoc != null) do {
				if (entry != null)
					pdoc = entry.getValue();
				
				if ((file = pdoc.getTextFile()) != null) try {
					tfm.releaseTempFile(file);
				} catch (FileNotFoundException e) {
					final String msg = (entry == null) ? "parser-document" : "sub parser-document '" + entry.getKey() + "'";
					logger.warn(String.format("data of %s of cmd [%06d] not available for release", msg, id));
				}
				pdocs.addAll(pdoc.getSubDocs().entrySet());
			} while ((entry = pdocs.poll()) != null);
			
		} catch (IOException e) { logger.error("I/O error during release of temporary files", e); }
	}
	
	public void handleEvent(Event event) {
		final Long id = (Long)event.getProperty(CommandEvent.PROP_COMMAND_ID);
		final ICommand cmd = tracker.getCommandByID(id);
		if (cmd != null)
			releaseCommandFiles(cmd, id);
	}
}
