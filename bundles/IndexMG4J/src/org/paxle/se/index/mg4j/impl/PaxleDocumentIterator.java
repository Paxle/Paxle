package org.paxle.se.index.mg4j.impl;

import it.unimi.dsi.mg4j.document.Document;
import it.unimi.dsi.mg4j.document.DocumentIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.paxle.core.queue.ICommand;

public class PaxleDocumentIterator implements DocumentIterator {
	private Iterator<ICommand> docIterator = null;
	
	public PaxleDocumentIterator(List<ICommand> indexerDocs) {
		if (indexerDocs == null) throw new NullPointerException("The list of indexer-documents is null");
		this.docIterator = indexerDocs.iterator();
	}
	
	/**
	 * @see DocumentIterator#nextDocument()
	 */
	public Document nextDocument() throws IOException {
		if (docIterator == null) return null;
		else if (!docIterator.hasNext()) return null;
		
		ICommand nextCommand = this.docIterator.next();
		return new PaxleDocument(nextCommand);
	}

	/**
	 * @see DocumentIterator#close()
	 */
	public void close() throws IOException {
		this.docIterator = null;
	}

}
