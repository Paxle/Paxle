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
package org.paxle.core.doc.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.paxle.core.doc.IParserDocument;
import org.paxle.core.io.temp.ITempFileManager;

public final class CachedParserDocument extends AParserDocument implements IParserDocument {
	
	private static final int DEFAULT_MAX_TEXT_SIZE_RAM = 1 * 1024 * 1024; // 1 mio. characters == 2 MB
	
	protected File content;
	protected OutputStreamWriter text;
	protected OutputStream os;
	
	public CachedParserDocument(ITempFileManager tfm) throws IOException {
		this(tfm, DEFAULT_MAX_TEXT_SIZE_RAM);
	}
	
	public CachedParserDocument(ITempFileManager tfm, int threshold) throws IOException {
		this.content = tfm.createTempFile();
		this.os = new InMemoryOutputStream(threshold, this.content);
		this.text = new OutputStreamWriter(this.os,"UTF-8");
	}	
	
	@Override
	@Deprecated
	public void addText(CharSequence text) throws IOException {
		if (text == null) return;
		this.append(text);
	}
	
	@Override
	public Appendable append(char c) throws IOException {
		this.text.append(c);
		return this;
	}
	
	@Override
	public Appendable append(CharSequence csq) throws IOException {
		this.text.append(csq);
		return this;
	}
	
	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		this.text.append(csq, start, end);
		return this;
	}
		
	@Override
	public void setTextFile(File file) throws IOException {
		this.content = file;
		this.os = new FileOutputStream(this.content);
		this.text = new OutputStreamWriter(this.os,"UTF-8");
	}
		
	@Override
	public File getTextFile() throws IOException {
		this.text.close();
		if (this.os instanceof InMemoryOutputStream) {
			final InMemoryOutputStream dos = (InMemoryOutputStream)os;
			if (dos.isInMemory()) {
				// writing data into a file
				FileOutputStream fout = new FileOutputStream(this.content);
				dos.writeTo(fout);
				fout.close();		
			} 
		}
		
		return this.content;
	}	
			
	@Override
	public Reader getTextAsReader() throws IOException {
		this.text.close();
		if (this.os instanceof InMemoryOutputStream) {
			final InMemoryOutputStream dos = (InMemoryOutputStream)os;
			if (dos.isInMemory()) {
				return new InputStreamReader(new ByteArrayInputStream(dos.getData()),Charset.forName("UTF-8"));
			} else {
				return new FileReader(this.content);
			}
		} else {
			return new FileReader(this.content);
		}
	}

	@Override
	public void close() throws IOException {
		this.text.close();
	}
	
	/**
	 * This function is used for testing only
	 */
	boolean inMemory() {
		if (this.os instanceof InMemoryOutputStream) {
			return ((InMemoryOutputStream)this.os).isInMemory();
		}
		return false;
	}
	
	/**
	 * TODO: we could implement a dynamic threshold here.
	 * if there is enough free memory available for the runtime
	 * we could allow to allocate more memory
	 */	
	private class InMemoryOutputStream extends DeferredFileOutputStream {

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
	}
}
