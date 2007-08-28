
package org.paxle.crawler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.paxle.core.charset.ACharsetDetectorOutputStream;
import org.paxle.core.charset.ICharsetDetector;
import org.paxle.core.crypt.md5.AMD5OutputStream;
import org.paxle.core.crypt.md5.IMD5;
import org.paxle.core.doc.ICrawlerDocument;

public class CrawlerTools {
	
	public static long saveInto(ICrawlerDocument doc, InputStream is) throws IOException {
		final File file = createTempFile(doc.getLocation(), CrawlerTools.class);
		OutputStream os = new FileOutputStream(file);
		ACharsetDetectorOutputStream chardetos = null;
		AMD5OutputStream md5os = null;
		
		final CrawlerContext context = CrawlerContext.getCurrentContext();
		if (context != null) {
			final ICharsetDetector chardet = context.getCharsetDetector();
			if (chardet != null) {
				chardetos = chardet.createOutputStream(os);
				os = chardetos;
			}
			
			final IMD5 md5 = context.getMD5();
			if (md5 != null) {
				md5os = md5.createOutputStream(os);
				os = md5os;
			}
			
			// TODO: mimetype detection
		}
		
		try {
			// TODO: mimetype detection during copying / cancel if not supported
			final long copied = copy(is, os);
			
			if (chardetos != null) {
				final String charset = chardetos.getCharset();
				if (charset != null)
					doc.setCharset(charset);
			}
			
			if (md5os != null) {
				final byte[] md5sum = md5os.getHash();
				if (md5sum != null)
					doc.setMD5Sum(md5sum);
			}
			
			// TODO: mimetype detection
			
			doc.setContent(file);
			return copied;
		} finally { os.close(); }
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
				if (cs == 0)
					break;
			}
		}
		os.flush();
		return rt;
	}
	
	// from YaCy: de.anomic.plasma.parser.AbstractParser
	public static File createTempFile(String name, Class clazz) throws IOException {
		final String parserClassName = clazz.getSimpleName();
		
		if (name == null) name = CrawlerTools.class.getName();
		// getting the file extension
		int idx = name.lastIndexOf("/");
		final String fileName = (idx != -1) ? name.substring(idx+1) : name;        
		
		idx = fileName.lastIndexOf(".");
		final String fileExt = (idx > -1) ? fileName.substring(idx+1) : "";
		
		// creates the temp file
		final File tempFile = File.createTempFile(
				parserClassName + "_" + ((idx > -1) ? fileName.substring(0, idx) : fileName),
				(fileExt.length() > 0) ? "." + fileExt : fileExt);
		return tempFile;
	}
}
