
package org.paxle.parser.iotools;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.paxle.core.io.IOTools;
import org.paxle.core.io.temp.ITempFileManager;

public class CachedWriter extends Writer {
	
	private static class CAOS extends CharArrayWriter {
		
		public CAOS() {
			super();
		}
		
		public CAOS(int initialSize) {
			super(initialSize);
		}
		
		public char[] getBuffer() {
			return super.buf;
		}
	}
	
	private final int maxSize;
	private final ITempFileManager tfm;
	private final Log logger = LogFactory.getLog(CachedWriter.class);
	private Writer writer;
	private long written = 0;
	private File ffile = null;
	private boolean fileWriterClosed = false;
	
	public CachedWriter(int maxSize, ITempFileManager tfm) {
		if (maxSize < 0)
			throw new IllegalArgumentException("max size < 0: " + maxSize);
		this.maxSize = maxSize;
		this.writer = new CAOS();
		this.tfm = tfm;
	}
	
	public CachedWriter(int maxSize, int initialSize, ITempFileManager tfm) throws IOException {
		if (maxSize < 0)
			throw new IllegalArgumentException("max size < 0: " + maxSize);
		this.maxSize = maxSize;
		this.tfm = tfm;
		if (initialSize > maxSize) {
			this.ffile = tfm.createTempFile();
			this.writer = new OutputStreamWriter(new FileOutputStream(this.ffile),"UTF-8");
		} else {
			this.writer = new CAOS(initialSize);
		}
	}
	
	public CachedWriter(File file) throws IOException {
		this.maxSize = -1;
		final RAFOutStream rafos = new RAFOutStream(file);
		rafos.seekAbsolute(file.length());
		this.writer = new OutputStreamWriter(rafos);
		this.tfm = null;
	}
	
	private void checkSize(int additional) throws IOException {
		if (this.written + additional > this.maxSize) {
			fallback(null);
		}
		this.written += additional;
	}
	
	public boolean isFallback() {
		return !(this.writer instanceof CAOS);
	}
	
	private void fallback(File file) throws IOException {
		if (isFallback()) return;
		if (file == null)
			file = this.tfm.createTempFile();
		this.writer.flush();
		this.writer.close();
		final CAOS caos = (CAOS)this.writer;
		this.ffile = file;
		this.writer = new OutputStreamWriter(new FileOutputStream(file),"UTF-8");
		IOTools.copy(new CharArrayReader(caos.getBuffer()), this.writer, this.written);
	}
	
	@Override
	public void close() throws IOException {
		if (fileWriterClosed) {
			logger.error("File Writer is closed allready");
			Thread.dumpStack();
		}
		flush();
		this.writer.close();
		if (isFallback())
			fileWriterClosed = true;
	}
	
	@Override
	public void flush() throws IOException {
		this.writer.flush();
	}
	
	@Override
	public void write(int c) throws IOException {
		checkSize(1);
		this.writer.write(c);
	}
	
	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {
		checkSize(len);
		this.writer.write(cbuf, off, len);
	}
	
	public File toFile(File file) throws IOException {
		if (!isFallback()) {
			fallback(file);
			close();
		}
		return this.ffile;
	}
	
	public Reader toReader() throws IOException {
		if (isFallback()) {
			return new FileReader(this.ffile);
		} else {
			close();
			return new CharArrayReader(((CAOS)this.writer).getBuffer());
		}
	}
	
	public long length() {
		return this.written;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (ffile != null) {
			if (isFallback() && !fileWriterClosed)
				writer.close();
			tfm.releaseTempFile(ffile);
		}
		super.finalize();
	}
	
	@Override
	public String toString() {
		if (isFallback()) try {
			close();
		} catch (IOException e) {
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
			sb.append(((CAOS)this.writer).getBuffer(), 0, size);
		}
		return sb.toString();
	}
}
