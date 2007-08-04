package org.paxle.se;

import java.io.IOException;

import org.paxle.core.doc.IIndexerDocument;

public interface IIndexWriter {
	
	public void write(IIndexerDocument document) throws IOException, IndexException;
}
