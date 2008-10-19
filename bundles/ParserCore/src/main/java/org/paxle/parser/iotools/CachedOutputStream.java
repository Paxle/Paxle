/*
This file is part of the Paxle project.
Visit http://www.paxle.net for more information.
Copyright 2007-2008 the original author or authors.

Licensed under the terms of the Common Public License 1.0 ("CPL 1.0"). 
Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt 
or in the file LICENSE.txt in the root directory of the Paxle distribution.

Unless required by applicable law or agreed to in writing, this software is distributed
on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*/

package org.paxle.parser.iotools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
		if (maxSize < 0)
			throw new IllegalArgumentException("max size < 0: " + maxSize);
		this.maxSize = maxSize;
		this.os = new BAOS();
		this.tfm = tfm;
	}
	
	public CachedOutputStream(int maxSize, long initialSize, ITempFileManager tfm) throws IOException {
		if (maxSize < 0)
			throw new IllegalArgumentException("max size < 0: " + maxSize);
		this.maxSize = maxSize;
		this.tfm = tfm;
		if (initialSize > maxSize) {
			this.ffile = tfm.createTempFile();
			this.os = new BufferedOutputStream(new FileOutputStream(this.ffile));
		} else {
			this.os = new BAOS((int)initialSize);
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
		return !(this.os instanceof BAOS);
	}
	
	private void fallback(File file) throws IOException {
		if (isFallback()) return;
		if (file == null)
			file = this.tfm.createTempFile();
		this.os.flush();
		this.os.close();
		final BAOS baos = (BAOS)this.os;
		this.ffile = file;
		this.os = new BufferedOutputStream(new FileOutputStream(file));
		IOTools.copy(new ByteArrayInputStream(baos.getBuffer()), this.os, this.written);
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
	
	public byte[] toBytes() {
		if (isFallback())
			throw new IllegalStateException("already in fallback-mode, can't access buffer");
		final BAOS baos = ((BAOS)os);
		final byte[] r = new byte[baos.size()];
		System.arraycopy(baos.getBuffer(), 0, r, 0, baos.size());
		return r;
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
			return new BufferedInputStream(new FileInputStream(this.ffile));
		} else {
			close();
			return new ByteArrayInputStream(((BAOS)this.os).getBuffer(), 0, ((BAOS)this.os).size());
		}
	}
	
	public long length() {
		return this.written;
	}
	
	@Override
	protected void finalize() throws Throwable {
		if (ffile != null) {
			if (isFallback() && !fileOSClosed)
				os.close();
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
			sb.append(new String(((BAOS)this.os).getBuffer(), 0, size));
		}
		return sb.toString();
	}
}
