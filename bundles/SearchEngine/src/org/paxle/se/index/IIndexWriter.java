package org.paxle.se.index;

import java.io.Closeable;
import java.io.IOException;

import org.paxle.core.doc.IIndexerDocument;
import org.apache.lucene.index.Term;

public interface IIndexWriter extends Closeable {
	
    public void write(IIndexerDocument document) throws IOException, IndexException;
    public void delete(Term term) throws IOException, IndexException;
}
