/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 * 
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.crawler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.concurrent.Semaphore;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.ICryptManager;
import org.paxle.core.charset.ACharsetDetectorOutputStream;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.crypt.ACryptOutputStream;
import org.paxle.core.crypt.ICrypt;
import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.io.IOTools;
import org.paxle.core.mimetype.IMimeTypeDetector;
import org.paxle.crawler.impl.ContentLengthLimitOutputStream;

public class CrawlerTools {
	
	private static final Log logger = LogFactory.getLog(CrawlerTools.class);
	
	public static class LimitedRateCopier {
		
		private final Semaphore sem = new Semaphore(1);
		private final long minDelta;
		private int working = 0;
		private volatile long curDelta;
		
		public LimitedRateCopier(final int maxKBps) {
			this.minDelta = 1000l / (maxKBps * 1024 / DEFAULT_BUFFER_SIZE_BYTES);
		}
		
		private int addWorker() throws InterruptedException {
			final int r;
			sem.acquire();
			r = ++working;
			sem.release();
			curDelta = minDelta * r;
			return r;
		}
		
		private int delWorker() throws InterruptedException {
			final int r;
			sem.acquire();
			r = --working;
			sem.release();
			curDelta = minDelta * r;
			return r;
		}
		
		public long copy(final InputStream is, final OutputStream os, final long bytes) throws IOException {
			long rt = 0;
			try {
				final byte[] buf = new byte[DEFAULT_BUFFER_SIZE_BYTES];
				int cs = (int)((bytes > 0 && bytes < DEFAULT_BUFFER_SIZE_BYTES) ? bytes : DEFAULT_BUFFER_SIZE_BYTES);
				
				int rn;
				long time = System.currentTimeMillis();
				addWorker();
				try {
					while ((rn = is.read(buf, 0, cs)) > 0) {
						os.write(buf, 0, rn);
						rt += rn;
						
						time += curDelta;
						final long sleep = time - System.currentTimeMillis();
						if (sleep > 0)
							Thread.sleep(sleep);
						
						if (bytes > 0) {
							cs = (int)Math.min(bytes - rt, DEFAULT_BUFFER_SIZE_BYTES);
							if (cs == 0)
								break;
						}
					}
				} finally { delWorker(); }
				os.flush();
			} catch (InterruptedException e) { /* ignore, simply quit copying */ }
			return rt;
		}
	}
	
	public static long saveInto(ICrawlerDocument doc, InputStream is) throws IOException {
		return saveInto(doc, is, null);
	}
	
	public static long saveInto(ICrawlerDocument doc, InputStream is, final LimitedRateCopier lrc) throws IOException {
		return saveInto(doc, is, lrc, -1);
	}
	
	/**
	 * Copies all data from the given {@link InputStream} to the given {@link ICrawlerDocument crawler-document}.<br />
	 * Additionally this function ...
	 * <ul>
	 * 	<li>detects the charset-encoding of the content using an {@link ICharsetDetector}</li>
	 *  <li>detects the mime-type of the content using an {@link IMimeTypeDetector}</li>
	 *  <li>generates a MD5 checksum of the content using an {@link ICryptManager}</li>
	 * </ul>
	 * 
	 * @see #copy(InputStream, OutputStream, long) for details
	 * @param doc the crawler-document the content belongs to
	 * @param is the stream to read from
	 * @param lrc the {@link LimitedRateCopier} to use to copy the data, if any, otherwise <code>null</code>
	 * @param maxFileSize max allowed content-size to copy in bytes or <code>-1</code>
	 * @return the number of copied bytes
	 * 
	 * @throws IOException if an I/O-error occures
	 * @throws ContentLengthLimitExceededException if the content-length read via the input stream exceeds the
	 * 	limit defined via maxFileSize
	 */
	public static long saveInto(ICrawlerDocument doc, InputStream is, final LimitedRateCopier lrc, final int maxFileSize) 
		throws IOException, ContentLengthLimitExceededException 
	{
		if (doc == null) throw new NullPointerException("The crawler-document is null.");
		if (is == null) throw new NullPointerException("The content inputstream is null.");
		
		final CrawlerContext context = CrawlerContext.getCurrentContext();
		if (context == null) throw new RuntimeException("Unexpected error. The crawler-context was null.");
		
		
		// testing if the charset is supported by java
		String contentCharset = doc.getCharset();
		
		boolean unsupportedCharset = false;		
		try {
			if (contentCharset != null && !Charset.isSupported(contentCharset)) {
				unsupportedCharset = true;
			}
		} catch (IllegalCharsetNameException e) {
			unsupportedCharset = true;
		}
		
		if (unsupportedCharset) {
			logger.warn(String.format(
					"The resource '%s' has an unsupported charset '%s'. Resetting charset ...", 
					doc.getLocation(),
					contentCharset
			));
			doc.setCharset(null);
		}
		
		File file = null;
		OutputStream os = null;
		try {
			// init file output-stream
			file = context.getTempFileManager().createTempFile();
			os = new BufferedOutputStream(new FileOutputStream(file));
			
			// limit file size
			ContentLengthLimitOutputStream tos = null;
			if(maxFileSize != -1) {
				tos = new ContentLengthLimitOutputStream(maxFileSize, os);
				os = tos;
			}
			
			// init charset detection stream
			ACharsetDetectorOutputStream chardetos = null;
			final ICharsetDetector chardet = context.getCharsetDetector();
			if (chardet != null) {
				chardetos = chardet.createOutputStream(os);
				os = chardetos;
			}
			
			// init md5-calculation stream
			ACryptOutputStream md5os = null;		
			ICryptManager cryptManager = context.getCryptManager();
			final ICrypt md5 = (cryptManager==null)?null:cryptManager.getCrypt("md5");
			if (md5 != null) {
				md5os = md5.createOutputStream(os);
				os = md5os;
			}
		
			/* ================================================================
			 * COPY DATA
			 * ================================================================ */
			final long copied = (lrc == null) ? IOTools.copy(is, os) : lrc.copy(is, os, -1);
			
			/* ================================================================
			 * CHARSET DETECTION
			 * ================================================================ */
			if (chardetos != null) {
				final String charset = chardetos.getCharset();
				if (charset != null) {
					doc.setCharset(charset);
					logger.debug(String.format("Charset of resource '%s' was detected as '%s'.",
							doc.getLocation(),
							contentCharset
					));
				}
			}
			
			/* ================================================================
			 * MD5-HASH
			 * ================================================================ */			
			if (md5os != null) {
				final byte[] md5sum = md5os.getHash();
				if (md5sum != null)
					doc.setMD5Sum(md5sum);
			}
			
			/* ================================================================
			 * MIME-TYPE DETECTION
			 * ================================================================ */
			if (doc.getMimeType() == null) {
				IMimeTypeDetector mimeTypeDetector = context.getMimeTypeDetector();
				if (mimeTypeDetector != null) {
					String mimeType = null;
					try {
						mimeType = mimeTypeDetector.getMimeType(file);
					} catch (Exception e) {						
						logger.warn(String.format(
								"Unexpected '%s' while trying to determine the mime-type of resource '%s'.",
								e.getClass().getName(),
								doc.getLocation()
						),e);
					}
					logger.debug(String.format("MimeType of resource '%s' was detected as '%s'", doc.getLocation(), mimeType));
					if (mimeType != null) 
						doc.setMimeType(mimeType);
				}
			}
			
			doc.setContent(file);
			return copied;
		} catch (Throwable e) {
			if (file != null) {
				// closing stream
				try {
					os.close();
				} catch (Exception e2) {
					/* ignore this */ 
				} finally { 
					os = null; 
				}
				
				// releasing temp file
				context.getTempFileManager().releaseTempFile(file);
			}
			
			// re-throw IO-Exceptions
			if (e instanceof IOException) throw (IOException) e;
			
			// convert other exceptions
			IOException ioe = new IOException(e.getMessage());
			ioe.initCause(e);
			throw ioe;
		} finally { 
			if (os != null) os.close(); 
		}
	}
	
	public static final int DEFAULT_BUFFER_SIZE_BYTES = 1024;
	
	/**
	 * Copies all data from the given {@link InputStream} to the given {@link OutputStream}
	 * using the {@link #copy(InputStream, OutputStream, long)}-method.
	 * <p><i>Note: this method does neither close the supplied InputStream nor the OutputStream.</i></p>
	 * 
	 * @see #copy(InputStream, OutputStream, long) for details
	 * @param is the stream to read from
	 * @param os the stream to write to
	 * @return the number of copied bytes
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public static long copy(InputStream is, OutputStream os) throws IOException {
		return copy(is, os, -1);
	}
	
	/**
	 * Copies an amount of data from the given {@link InputStream} to the given {@link OutputStream}.
	 * <p><i>Note: this method does neither close the supplied InputStream nor the OutputStream.</i></p>
	 * 
	 * @see #DEFAULT_BUFFER_SIZE_BYTES for the size of the buffer
	 * @see InputStream#read(byte[], int, int)
	 * @see OutputStream#write(byte[], int, int)
	 * @param is the stream to read from
	 * @param os the stream to write to
	 * @param bytes the number of bytes to copy
	 * @return the number of copied bytes
	 * @throws <b>IOException</b> if an I/O-error occures
	 */
	public static long copy(InputStream is, OutputStream os, long bytes) throws IOException {
		final byte[] buf = new byte[DEFAULT_BUFFER_SIZE_BYTES];                
		int cs = (int)((bytes > 0 && bytes < DEFAULT_BUFFER_SIZE_BYTES) ? bytes : DEFAULT_BUFFER_SIZE_BYTES);
		
		int rn;
		long rt = 0;
		while ((rn = is.read(buf, 0, cs)) > 0) {
			os.write(buf, 0, rn);
			rt += rn;
			
			if (bytes > 0) {
				cs = (int)Math.min(bytes - rt, DEFAULT_BUFFER_SIZE_BYTES);
				if (cs == 0) break;
			}
		}
		os.flush();
		return rt;
	}
}
