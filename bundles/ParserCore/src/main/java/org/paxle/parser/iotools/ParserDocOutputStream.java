
package org.paxle.parser.iotools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.charset.ACharsetDetectorOutputStream;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.ParserException;

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
public class ParserDocOutputStream extends OutputStream {
	
	private static final Log logger = LogFactory.getLog(ParserDocOutputStream.class);
	
	private boolean closed = false;
	private final ITempFileManager tfm;
	private final OutputStream os;
	private final File of;
	
	// TODO: cache file in ram
	public ParserDocOutputStream(ITempFileManager tfm, ICharsetDetector cd) throws IOException {
		this.tfm = tfm;
		this.of = tfm.createTempFile();
		final OutputStream fos = new BufferedOutputStream(new FileOutputStream(this.of));
		this.os = (cd != null) ? cd.createOutputStream(fos) : fos;
	}
	
	@Override
	public void write(int b) throws IOException {
		this.os.write(b);
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		this.os.write(b, off, len);
	}
	
	@Override
	public void close() throws IOException {
		this.os.flush();
		this.os.close();
		closed = true;
	}
	
	public String getCharset() {
		if (this.os instanceof ACharsetDetectorOutputStream) {
			return ((ACharsetDetectorOutputStream)this.os).getCharset();
		} else {
			return null;
		}
	}
	
	public IParserDocument parse(URI location) throws ParserException, IOException {
		/* Closing stream if not already done.
		 * 
		 * ATTENTION: don't remove the closed check. Otherwise we get an StackOverflowException, because
		 *            SubParserDocOutputStream is overwriting close and calls parse again within close! 
		 */
		if (!closed) this.close();
		
		final String charset = getCharset();
		final String mimeType = ParserTools.getMimeType(this.of);
		logger.debug(String.format("Parsing contained file in '%s' with mime-type '%s' and charset '%s'", location, mimeType, charset));
		try {
			return ParserTools.parse(location, mimeType, charset, this.of);
		} catch (UnsupportedEncodingException e) {
			throw new ParserException("Error parsing file on close due to incorrectly detected charset '" + charset + "'", e);
		} finally {
			this.tfm.releaseTempFile(this.of);
		}
	}
	
	@Override
	public void flush() throws IOException {
		this.os.flush();
	}
}
