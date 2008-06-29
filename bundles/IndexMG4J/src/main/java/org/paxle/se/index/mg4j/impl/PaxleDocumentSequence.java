package org.paxle.se.index.mg4j.impl;

import it.unimi.dsi.mg4j.document.AbstractDocumentSequence;
import it.unimi.dsi.mg4j.document.DocumentFactory;
import it.unimi.dsi.mg4j.document.DocumentIterator;
import it.unimi.dsi.mg4j.document.DocumentSequence;
import it.unimi.dsi.io.SafelyCloseable;

import java.io.IOException;
import java.util.List;

import org.paxle.core.queue.ICommand;

public class PaxleDocumentSequence extends AbstractDocumentSequence implements DocumentSequence, SafelyCloseable {

	/** The factory to be used by this collection. */
	private final DocumentFactory factory;
	
	private PaxleDocumentIterator documentIterator = null;
	
	public PaxleDocumentSequence(DocumentFactory factory, List<ICommand> indexerDocs) {
		if (factory == null) throw new NullPointerException("The document-factory is null");
		if (indexerDocs == null) throw new NullPointerException("The list of indexer-documents is null");
		
		this.factory = factory;
		this.documentIterator = new PaxleDocumentIterator(indexerDocs);
	}

	/**
	 * @see DocumentSequence#factory()
	 */
	public DocumentFactory factory() {
		return this.factory;
	}

	/**
	 * @see DocumentSequence#iterator()
	 */
	public DocumentIterator iterator() throws IOException {
		return this.documentIterator;
	}

}
