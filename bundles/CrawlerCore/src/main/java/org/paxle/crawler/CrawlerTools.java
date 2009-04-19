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
package org.paxle.crawler;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
	
	/**
	 * Describes an entry in a dir-listing. Only one of the methods {@link DirlistEntry#getFileURI()}
	 * and {@link DirlistEntry#getFileName()} needs to return something valid.
	 */
	public static interface DirlistEntry {
		
		public abstract URI getFileURI();
		public abstract String getFileName();
		public abstract long getSize();
		public abstract long getLastModified();
	}
	
	/**
	 * Uses compression
	 * @see CrawlerTools#generateListing(FileListIt, String, boolean)
	 * @return the listing as {@link InputStream}
	 */
	public static InputStream generateListing(final Iterator<DirlistEntry> fileListIt, final String location) {
		return generateListing(fileListIt, location, true);
	}
	
	/**
	 * Generates a file-listing in a standard format understood by Paxle. Currently this format
	 * consists of a rudimentary HTML-page linking to the files in the list given by
	 * <code>fileListIt</code>. The resulting format of this list not yet finalized and subject
	 * to change.
	 * 
	 * @param fileListIt the file-listing providing the required information to include in the result
	 * @param location the location where this listing has been obtained
	 * @param compress determines whether the content should be compressed transparently (via GZip)
	 *        to save memory. The format of the result is not impaired as the data is being decompressed
	 *        during reading. Compression reduces the size of the representation of large directories
	 *        up to a sixth.
	 * @return an {@link InputStream} containing the binary representation in UTF-8 encoding of the
	 *         resulting listing. It can be fed directly into any of the
	 *         <code>CrawlerTools.saveInto()</code>-methods
	 */
	public static InputStream generateListing(final Iterator<DirlistEntry> fileListIt, final String location, boolean compress) {
		final class BAOS extends ByteArrayOutputStream {
			
			public byte[] getBuffer() {
				return super.buf;
			}
		}
		
		final BAOS baos = new BAOS();
		OutputStream writerOut = baos;
		if (compress) try {
			writerOut = new GZIPOutputStream(baos);
		} catch (IOException e) {
			throw new RuntimeException("I/O error when using gzip upon a byte-array-output-stream for " + location, e);
		}
		
		final Formatter writer;
		try {
			writer = new Formatter(writerOut, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("UTF-8 not supported, WTF?", e);
		}
		
		// getting the base dir
		String baseURL = location;
		if (!baseURL.endsWith("/")) baseURL += "/";
		
		// getting the parent dir
		String parentDir = "/";
		if (baseURL.length() > 1) {
			parentDir = baseURL.substring(0,baseURL.length()-1);
			int idx = parentDir.lastIndexOf("/");
			parentDir = parentDir.substring(0,idx+1);
		}
		
		writer.format("<html><head><title>Index of %s</title></head><hr><table><tbody>\r\n", location);
		writer.format("<tr><td colspan=\"3\"><a href=\"%s\">Up to higher level directory</a></td></tr>\r\n",parentDir);
		
		// generate directory listing
		// FIXME: we need to escape the urls properly here.
		while (fileListIt.hasNext()) {
			final DirlistEntry entry = fileListIt.next();
			final String nexturi;
			if (entry.getFileURI() == null) {
				nexturi = baseURL + entry.getFileName();
			} else {
				nexturi = entry.getFileURI().toASCIIString();
			}
			writer.format(
						"<tr>" +
							"<td><a href=\"%1$s\">%2$s</a></td>" +
							"<td>%3$d Bytes</td>" +
							"<td>%4$tY-%4$tm-%4$td %4$tT</td>" +
						"</tr>\r\n",
					nexturi,
					entry.getFileName(),
					Long.valueOf(entry.getSize()),
					Long.valueOf(entry.getLastModified())
			);
		}
		
		writer.format("</tbody></table><hr></body></html>");
		writer.close();
		
		InputStream ret = new ByteArrayInputStream(baos.getBuffer(), 0, baos.size());
		if (compress) try {
			ret = new GZIPInputStream(ret);
		} catch (IOException e) {
			throw new RuntimeException("I/O error when using gzip upon a byte-array-input-stream for " + location, e);
		}
		return ret;
	}
	
	/**
	 * This function tests if the given charset is supported by the java runtime. 
	 * 
	 * @param contentCharset the charset to test
	 * @return <code>true</code> if the charset is not supported
	 */
	static boolean isUnsupportedCharset(String contentCharset) {		
		boolean unsupportedCharset = true;		

		try {
			if (contentCharset != null && Charset.isSupported(contentCharset)) {
				unsupportedCharset = false;
			}
		} catch (IllegalCharsetNameException e) {}
		
		return unsupportedCharset;
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
		if (doc.getCharset() != null && isUnsupportedCharset(doc.getCharset())) {
			logger.warn(String.format(
					"The resource '%s' has an unsupported charset '%s'. Resetting charset ...", 
					doc.getLocation(),
					doc.getCharset()
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
				if (charset == null || charset.length() == 0) {
					logger.debug(String.format(
							"The CharsetDetector was unable to determine the charset for resource '%s'.",
							doc.getLocation()
					));
				} else if (isUnsupportedCharset(charset)) {
					logger.warn(String.format(
							"The CharsetDetector detected an unsupported charset '%s' for resource '%s'.",
							charset,
							doc.getLocation()
					));
				} else {
					doc.setCharset(charset);
					logger.debug(String.format(
							"The CharsetDetector detected charset '%s' for resource '%s'.",
							charset,
							doc.getLocation()
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
