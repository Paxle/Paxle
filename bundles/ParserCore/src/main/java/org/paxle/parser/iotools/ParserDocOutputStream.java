/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.parser.iotools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.commons.io.output.DeferredFileOutputStream;
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
	
	private static final int MAX_CACHED_SIZE = 512 * 1024;
	
	private static final Log logger = LogFactory.getLog(ParserDocOutputStream.class);
	
	private boolean closed = false;
	private final OutputStream os;
	private final DeferredFileOutputStream cos;
	private final ITempFileManager tfm;
	
	public ParserDocOutputStream(final ITempFileManager tfm, final ICharsetDetector cd, final long expectedSize) throws IOException {
		this(tfm, cd);
	}
	
	public ParserDocOutputStream(final ITempFileManager tfm, final ICharsetDetector cd) throws IOException {
		this.tfm = tfm;
		File tempFile = this.tfm.createTempFile();
		
		this.cos = new DeferredFileOutputStream(MAX_CACHED_SIZE, tempFile);
		this.os = (cd == null) ? cos : cd.createOutputStream(cos);
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
	
	protected String getMimeType(final String logName) throws ParserException, IOException {
		return (!cos.isInMemory())
				? ParserTools.getMimeType(cos.getFile())
				: ParserTools.getMimeType(cos.getData(), logName);
	}
	
	public IParserDocument parse(URI location) throws ParserException, IOException {
		return parse(location, getMimeType(location.toString()));
	}
	
	public IParserDocument parse(URI location, final String mimeType) throws ParserException, IOException {
		/* Closing stream if not already done.
		 * 
		 * ATTENTION: don't remove the closed check. Otherwise we get an StackOverflowException, because
		 *            SubParserDocOutputStream is overwriting close and calls parse again within close! 
		 */
		if (!closed) this.close();
		
		String charset = getCharset();
		File dataFile = null;
		try {			
			logger.debug(String.format("Parsing contained file in '%s' with mime-type '%s' and charset '%s'", location, mimeType, charset));
			
			if (!cos.isInMemory()) {
				// the file created by cos.toFile(null) is being removed when cos is finalized
				dataFile = cos.getFile();
				return ParserTools.parse(location, mimeType, charset, dataFile);
			} else {
				ByteArrayInputStream bin = new ByteArrayInputStream(cos.getData());
				return ParserTools.parse(location, mimeType, charset, bin);
			}
		} catch (UnsupportedEncodingException e) {
			throw new ParserException("Error parsing file on close due to incorrectly detected charset '" + charset + "'", e);
		} finally {
			// release temp file
			this.tfm.releaseTempFile(dataFile);
		}
	}
	
	@Override
	public void flush() throws IOException {
		this.os.flush();
	}
}
