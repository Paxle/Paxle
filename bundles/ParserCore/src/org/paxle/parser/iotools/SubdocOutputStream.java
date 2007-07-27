
package org.paxle.parser.iotools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.paxle.core.doc.IParserDocument;

/**
 * This class is a wrapper around a {@link FileOutputStream} to save the written
 * data to a temp file. When the writing finishes, the resulting file on the disk
 * is being parsed and the resulting {@link IParserDocument} is added to the provided
 * one. Finally the temporary file is being deleted.
 * 
 * @see File#createTempFile(String, String)
 * @see FileOutputStream
 * @see ParserTools#parse(String, File)
 * @see IParserDocument#addSubDocument(String, IParserDocument)
 */
public class SubdocOutputStream extends OutputStream {
	
	private final FileOutputStream fos;
	private final IParserDocument pdoc;
	private final File of;
	private final String loc;
	
	// TODO: cache file in ram
	public SubdocOutputStream(IParserDocument pdoc, String location) throws IOException {
		this.pdoc = pdoc;
		this.loc = location;
		this.of = ParserTools.createTempFile(location, SubdocOutputStream.class);
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
		try {
			this.pdoc.addSubDocument(this.loc, ParserTools.parse(this.loc, this.of));
		} catch (Exception e) {
			System.out.println("error parsing inner file '" + this.loc + "', saved in '" + this.of + "': " + e);
			/* ignore as this is only a subdocument */
		} finally {
			this.of.delete();
		}
	}
	
	@Override
	public void flush() throws IOException {
		this.fos.flush();
	}
}
