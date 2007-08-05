package org.paxle.se.index.lucene.impl;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.index.IndexException;
import org.paxle.se.index.lucene.ILuceneWriter;

public class LuceneWriter extends IndexWriter implements ILuceneWriter {
	
	public static LuceneWriter createWriter(String dbpath) throws CorruptIndexException,
			LockObtainFailedException, IOException {
		return new LuceneWriter(dbpath, new StandardAnalyzer());
	}
	
	/** @see IndexWriter#IndexWriter(String, Analyzer) */
	public LuceneWriter(String arg0, Analyzer arg1) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1);
	}
	
	/** @see IndexWriter#IndexWriter(File, Analyzer) */
	public LuceneWriter(File arg0, Analyzer arg1) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, Analyzer) */
	public LuceneWriter(Directory arg0, Analyzer arg1) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1);
	}
	
	/** @see IndexWriter#IndexWriter(String, Analyzer, boolean) */
	public LuceneWriter(String arg0, Analyzer arg1, boolean arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(File, Analyzer, boolean) */
	public LuceneWriter(File arg0, Analyzer arg1, boolean arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, Analyzer, boolean) */
	public LuceneWriter(Directory arg0, Analyzer arg1, boolean arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer) */
	public LuceneWriter(Directory arg0, boolean arg1, Analyzer arg2) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer, boolean) */
	public LuceneWriter(
			Directory arg0,
			boolean arg1,
			Analyzer arg2,
			boolean arg3) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2, arg3);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer, IndexDeletionPolicy) */
	public LuceneWriter(
			Directory arg0,
			boolean arg1,
			Analyzer arg2,
			IndexDeletionPolicy arg3) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2, arg3);
	}
	
	/** @see IndexWriter#IndexWriter(Directory, boolean, Analyzer, boolean, IndexDeletionPolicy) */
	public LuceneWriter(
			Directory arg0,
			boolean arg1,
			Analyzer arg2,
			boolean arg3,
			IndexDeletionPolicy arg4) throws CorruptIndexException,
			LockObtainFailedException,
			IOException {
		super(arg0, arg1, arg2, arg3, arg4);
	}
	
	public synchronized void write(IIndexerDocument document) throws IOException, IndexException {
		try {
			super.addDocument(Converter.iindexerDoc2LuceneDoc(document));
		} catch (CorruptIndexException e) {
			throw new IndexException("error adding lucene document for " + document.get(IIndexerDocument.LOCATION) + " to index", e);
		} finally {
			// close everything now
			for (final Map.Entry<org.paxle.core.doc.Field<?>,Object> entry : document)
				if (Closeable.class.isAssignableFrom(entry.getKey().getType()))
					((Closeable)entry.getValue()).close();
		}
	}
}
