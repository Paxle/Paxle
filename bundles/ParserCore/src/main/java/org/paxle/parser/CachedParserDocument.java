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
package org.paxle.parser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.iotools.CachedWriter;

public final class CachedParserDocument extends ParserDocument implements IParserDocument {
	
	private static final int MAX_TEXT_SIZE_RAM = 1 * 1024 * 1024; // 1 mio. characters == 2 MB
	private CachedWriter text;
	
	public CachedParserDocument(ITempFileManager tfm) {
		this.text = new CachedWriter(MAX_TEXT_SIZE_RAM, tfm);
	}
	
	public CachedParserDocument(int initialTextSize, ITempFileManager tfm) throws IOException {
		this.text = new CachedWriter(MAX_TEXT_SIZE_RAM, initialTextSize, tfm);
	}
	
	@Override
	public void addText(CharSequence text) throws IOException {
		if (text == null) return;
		this.text.append(text.toString());
	}
	
	public void addText(String text) throws IOException {
		if (text == null) return;
		this.text.append(text);
	}
		
	@Override
	public void setTextFile(File file) throws IOException {
		this.content = file;
		this.text = new CachedWriter(this.content);
	}
	
	public void setText(CachedWriter resource) {
		this.content = null;
		this.text = resource;
	}
			
	@Override
	public Reader getTextAsReader() throws IOException {
		return this.text.toReader();
	}
	
	@Override
	public File getTextFile() throws IOException {
		// we need to do this because hibernate seems to get the text-file twice
		if (this.content == null) {
			this.content = this.text.toFile(null);
		}
		return this.content;
	}
	
	public File getTextFile(File file) throws IOException {
		this.content = this.text.toFile(file);
		return this.content;
	}	
	
	public Writer getTextWriter() {
		return this.text;
	}
	
	@Override
	public void close() throws IOException {
		this.text.close();
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append("Text:").append('\n').append(this.text);
		return sb.toString();
	}	
}
