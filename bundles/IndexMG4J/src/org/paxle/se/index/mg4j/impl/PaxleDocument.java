package org.paxle.se.index.mg4j.impl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.paxle.core.doc.IIndexerDocument;
import org.paxle.core.queue.ICommand;

import it.unimi.dsi.mg4j.document.Document;
import it.unimi.dsi.mg4j.io.FastBufferedReader;
import it.unimi.dsi.mg4j.io.NullReader;
import it.unimi.dsi.mg4j.io.WordReader;

public class PaxleDocument implements Document {
	private ICommand cmd = null;
	
	public PaxleDocument(ICommand cmd) {
		if (cmd == null) throw new NullPointerException("The command object is null");
		this.cmd = cmd;
	}
	
	public void close() throws IOException {
		// TODO close file reader
		System.out.println("closed");
	}

	public Object content(int field) throws IOException {
		// TODO Auto-generated method stub
		if (field == 0) {
			File textFile = cmd.getIndexerDocuments()[0].get(IIndexerDocument.TEXT);
			return new FileReader(textFile);
		} else if (field == 1) {
			String author = cmd.getIndexerDocuments()[0].get(IIndexerDocument.AUTHOR);
			if (author == null) return NullReader.getInstance();
			return new FastBufferedReader(author.toCharArray());
		}
		return null;
	}

	public CharSequence title() {
		// TODO Auto-generated method stub
		return null;
	}

	public CharSequence uri() {
		// TODO Auto-generated method stub
		return null;
	}

	public WordReader wordReader(int field) {
		return new FastBufferedReader();
	}

}
