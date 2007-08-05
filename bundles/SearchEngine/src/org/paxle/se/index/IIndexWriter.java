package org.paxle.se.index;

import java.io.Closeable;
import java.io.IOException;

import org.paxle.core.doc.IIndexerDocument;

public interface IIndexWriter extends Closeable {
	
	public void write(IIndexerDocument document) throws IOException, IndexException;
}
