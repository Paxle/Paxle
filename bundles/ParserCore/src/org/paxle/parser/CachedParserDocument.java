package org.paxle.parser;

import java.io.File;
import java.io.IOException;
import java.io.Reader;

import org.paxle.core.doc.IParserDocument;
import org.paxle.core.doc.ParserDocument;
import org.paxle.core.io.temp.ITempFileManager;
import org.paxle.parser.iotools.CachedWriter;

public final class CachedParserDocument extends ParserDocument implements IParserDocument {
	
	private static final int MAX_TEXT_SIZE_RAM = 2 * 1024 * 1024; // 2 mio. characters == 4 MB
	private CachedWriter text;
	
	public CachedParserDocument(ITempFileManager tfm) {
		this.text = new CachedWriter(MAX_TEXT_SIZE_RAM, tfm);
	}
	
	public CachedParserDocument(int initialTextSize, ITempFileManager tfm) throws IOException {
		this.text = new CachedWriter(MAX_TEXT_SIZE_RAM, initialTextSize, tfm);
	}
	
	@Override
	public void addText(CharSequence text) throws IOException {
		this.text.append(text.toString());
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
