/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.core.doc.impl;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;

public final class CachedParserDocument extends AParserDocument implements IParserDocument {
	
	private static final int DEFAULT_MAX_TEXT_SIZE_RAM = 1 * 1024 * 1024; // 1 mio. characters == 2 MB
	
	protected OutputStream contentStream;
	protected int inMemoryThreshold;
	
	public CachedParserDocument(ITempFileManager tempFileManager) throws IOException {
		this(tempFileManager, DEFAULT_MAX_TEXT_SIZE_RAM);
	}
	
	public CachedParserDocument(ITempFileManager tempFileManager, int threshold) throws IOException {
		super(tempFileManager);
		this.inMemoryThreshold = threshold;
	}	
		
	@Override
	public void setTextFile(File file) throws IOException {
		// closing old streams
		this.close();
		
		// releasing old temp-file
		if (file != null && !file.equals(this.contentFile)) {
			if (this.tempFileManager != null && this.tempFileManager.isKnown(this.contentFile)) {
				this.tempFileManager.releaseTempFile(this.contentFile);
			}
		}
		
		// initiialize internal structure
		this.contentWriter = null;
		this.contentStream = null;		
		this.contentFile = file;
		
		if (file != null && file.exists() && file.length() > 0) {
			/* 
			 * If the file already contains data, we must not create an in-memory-writer.
			 * Therefore we create a simple FileOutputStream here 
			 */
			this.contentStream = new BufferedOutputStream(new FileOutputStream(this.contentFile,true));
		}
	}
		
	@Override
	public File getTextFile() throws IOException {
		this.close();
		if (this.inMemory() && this.contentStream != null) {
			final InMemoryOutputStream dos = (InMemoryOutputStream)this.contentStream;
			if (dos.getByteCount() == 0) return null;
				
			// writing data into a file
			OutputStream fout = null;
			try {
				fout = new BufferedOutputStream(new FileOutputStream(this.contentFile));
				dos.writeTo(fout);
			} finally {
				if (fout != null) fout.close();
			}

			this.contentWriter = null;
			this.contentStream = null;
		}
		
		if (this.contentFile == null || !this.contentFile.exists() || this.contentFile.length() == 0) return null;
		return this.contentFile;
	}	
			
	@Override
	public Reader getTextAsReader() throws IOException {
		this.close();
		if (this.inMemory() && this.contentStream != null) {
			final InMemoryOutputStream dos = (InMemoryOutputStream)this.contentStream;
			if (dos.getByteCount() == 0) return null;
			return new InputStreamReader(new ByteArrayInputStream(dos.getData()),Charset.forName("UTF-8"));
		} 
		
		if (this.contentFile == null || !this.contentFile.exists() || this.contentFile.length() == 0) return null;
		return new InputStreamReader(new FileInputStream(this.contentFile),Charset.forName("UTF-8"));
	}

	public Writer getTextWriter() throws IOException {
		if (this.contentFile == null) {
			this.contentFile = this.tempFileManager.createTempFile();
		}
		if (this.contentFile != null && this.contentStream == null) {
			this.contentStream = new InMemoryOutputStream(this.inMemoryThreshold, this.contentFile);
		}
		if (this.contentFile != null && this.contentStream != null && this.contentWriter == null) {
			this.contentWriter = new DocumentWriter(new OutputStreamWriter(this.contentStream,"UTF-8"));
		}
		return this.contentWriter;
	}
	
	public long length() throws IOException {
		// flush data
		if (!this.closed && this.contentWriter != null) {
			this.contentWriter.flush();
		}
		
		// check for an in-memory-stream
		if (this.inMemory() && this.contentStream != null) {
			final InMemoryOutputStream dos = (InMemoryOutputStream)this.contentStream;
			return dos.getByteCount();
		}
		
		if (this.contentFile == null || !this.contentFile.exists()) return 0;
		return this.contentFile.length();		
	}
	
	/**
	 * Checks if this ParserDocument is held in memory or on disk
	 */
	@Override
	public boolean inMemory() {
		// no data was written yet.
		if (this.contentFile == null) return true;
			
		// check for an in-memory-stream
		if (this.contentStream != null && this.contentStream instanceof InMemoryOutputStream) {
			return ((InMemoryOutputStream)this.contentStream).isInMemory();
		}
		return false;
	}
	
	/**
	 * TODO: we could implement a dynamic threshold here.
	 * if there is enough free memory available for the runtime
	 * we could allow to allocate more memory
	 */	
	private static class InMemoryOutputStream extends DeferredFileOutputStream {

		/**
		 * @param threshold if more bytes than this value are written into the stream it is cached into file outputFile
		 * @param outputFile the file used for caching
		 */
		public InMemoryOutputStream(int threshold, File outputFile) {
			super(threshold, outputFile);
		}
		
		@Override
		protected void checkThreshold(int count) throws IOException {
			super.checkThreshold(count);
		}
		
		@Override
		public boolean isThresholdExceeded() {
			return super.isThresholdExceeded();
		}
		
		@Override
		public void close() throws IOException {
			super.close();
		}
	}
}
