package org.paxle.parser.iotools;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

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
	private Writer writer;
	private long written = 0;
	private File ffile = null;
	
	public CachedWriter(int maxSize, ITempFileManager tfm) {
		this.maxSize = maxSize;
		this.writer = new CAOS();
		this.tfm = tfm;
	}
	
	public CachedWriter(int maxSize, int initialSize, ITempFileManager tfm) throws IOException {
		this.maxSize = maxSize;
		this.tfm = tfm;
		if (initialSize > maxSize) {
			this.ffile = tfm.createTempFile();
			this.writer = new FileWriter(this.ffile);
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
		return (this.writer instanceof OutputStreamWriter);
	}
	
	private void fallback(File file) throws IOException {
		if (isFallback()) return;
		if (file == null)
			file = this.tfm.createTempFile();
		close();
		final CAOS caos = (CAOS)this.writer;
		this.ffile = file;
		this.writer = new FileWriter(file);
		ParserTools.copy(new CharArrayReader(caos.getBuffer()), this.writer, this.written);
	}
	
	@Override
	public void close() throws IOException {
		flush();
		this.writer.close();
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
		fallback(file);
		close();
		return this.ffile;
	}
	
	public Reader toReader() throws IOException {
		close();
		if (isFallback()) {
			return new FileReader(this.ffile);
		} else {
			return new CharArrayReader(((CAOS)this.writer).getBuffer());
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
			ParserTools.copy(fr, sb);
			fr.close();
		} catch (IOException e) {
			throw new RuntimeException("error copying text data from stream to memory", e);
		} else {
			sb.append(((CAOS)this.writer).getBuffer(), 0, size);
		}
		return sb.toString();
	}
}
