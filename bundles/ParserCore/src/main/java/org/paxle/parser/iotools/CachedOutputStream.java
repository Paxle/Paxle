
package org.paxle.parser.iotools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.ITempFileManager;

public class CachedOutputStream extends OutputStream {
	
	private static final class BAOS extends ByteArrayOutputStream {
		
		public BAOS() {
			super();
		}
		
		public BAOS(final int initialSize) {
			super(initialSize);
		}
		
		public byte[] getBuffer() {
			return super.buf;
		}
	}
	
	private final int maxSize;
	private final ITempFileManager tfm;
	private final Log logger = LogFactory.getLog(CachedOutputStream.class);
	private OutputStream os;
	private long written = 0;
	private File ffile = null;
	private boolean fileOSClosed = false;
	
	public CachedOutputStream(int maxSize, ITempFileManager tfm) {
		this.maxSize = maxSize;
		this.os = new BAOS();
		this.tfm = tfm;
	}
	
	public CachedOutputStream(int maxSize, int initialSize, ITempFileManager tfm) throws IOException {
		this.maxSize = maxSize;
		this.tfm = tfm;
		if (initialSize > maxSize) {
			this.ffile = tfm.createTempFile();
			this.os = new FileOutputStream(this.ffile);
		} else {
			this.os = new BAOS(initialSize);
		}
	}
	
	public CachedOutputStream(File file) throws IOException {
		this.maxSize = -1;
		final RAFOutStream rafos = new RAFOutStream(file);
		rafos.seekAbsolute(file.length());
		this.os = rafos;
		this.tfm = null;
	}
	
	private void checkSize(int additional) throws IOException {
		if (this.written + additional > this.maxSize) {
			fallback(null);
		}
		this.written += additional;
	}
	
	public boolean isFallback() {
		return (this.os instanceof RAFOutStream);
	}
	
	private void fallback(File file) throws IOException {
		if (isFallback()) return;
		if (file == null)
			file = this.tfm.createTempFile();
		this.os.flush();
		this.os.close();
		final BAOS caos = (BAOS)this.os;
		this.ffile = file;
		this.os = new FileOutputStream(file);
		IOTools.copy(new ByteArrayInputStream(caos.getBuffer()), this.os, this.written);
	}
	
	@Override
	public void close() throws IOException {
		if (fileOSClosed) {
			logger.error("File Writer is closed allready");
			Thread.dumpStack();
		}
		flush();
		this.os.close();
		if (isFallback())
			fileOSClosed = true;
	}
	
	@Override
	public void flush() throws IOException {
		this.os.flush();
	}
	
	@Override
	public void write(int c) throws IOException {
		checkSize(1);
		this.os.write(c);
	}
	
	@Override
	public void write(byte[] cbuf, int off, int len) throws IOException {
		checkSize(len);
		this.os.write(cbuf, off, len);
	}
	
	public File toFile(File file) throws IOException {
		if (!isFallback()) {
			fallback(file);
			close();
		}
		return this.ffile;
	}
	
	public InputStream toInputStream() throws IOException {
		if (isFallback()) {
			return new FileInputStream(this.ffile);
		} else {
			close();
			return new ByteArrayInputStream(((BAOS)this.os).getBuffer(), 0, ((BAOS)this.os).size());
		}
	}
	
	public long length() {
		return this.written;
	}
	
	@Override
	public String toString() {
		try { close(); } catch (IOException e) {
			throw new RuntimeException("error copying text data from stream to memory", e);
		}
		
		final int size;
		if (this.written > Integer.MAX_VALUE) {
			throw new OutOfMemoryError("error allocating " + this.written + " chars of memory");
		} else {
			size = (int)this.written;
		}
		
		final StringBuilder sb = new StringBuilder(size);
		if (isFallback()) try {
			final Reader fr = new FileReader(this.ffile);
			IOTools.copy(fr, sb);
			fr.close();
		} catch (IOException e) {
			throw new RuntimeException("error copying text data from stream to memory", e);
		} else {
			sb.append(new String(((BAOS)this.os).getBuffer(), 0, size));
		}
		return sb.toString();
	}
}
