
package org.paxle.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.paxle.core.doc.IParserDocument;

public class SubdocOutputStream extends OutputStream {
	
	private final FileOutputStream fos;
	private final IParserDocument pdoc;
	private final File of;
	private final String loc;
	
	// TODO: cache file in ram
	public SubdocOutputStream(IParserDocument pdoc, String location) throws IOException {
		this.pdoc = pdoc;
		this.loc = location;
		this.of = File.createTempFile("", ""); // ParserTools.createTempFile(SubdocOutputStream.class);
		this.fos = new FileOutputStream(this.of);
	}
	
	@Override
	public void write(int b) throws IOException {
		this.fos.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.fos.write(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		this.fos.close();
		/*
		try {
			this.pdoc.addSubDocument(this.loc, ParserTools.parseSubDoc(this.of));
		} catch (ParserException e) {
			throw new IOException("error parsing inner file '" + this.loc + "', saved in '" + this.of, e);
		}*/
	}
	
	@Override
	public void flush() throws IOException {
		this.fos.flush();
	}
}
