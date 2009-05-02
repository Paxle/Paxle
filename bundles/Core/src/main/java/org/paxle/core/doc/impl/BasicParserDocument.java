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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import org.paxle.core.io.temp.ITempFileManager;

public class BasicParserDocument extends AParserDocument {

	protected File content;
	protected OutputStreamWriter contentOut = null;
	protected final ITempFileManager tempFileManager;

	public BasicParserDocument(@Nonnull ITempFileManager tempFileManager) {
		this.tempFileManager = tempFileManager;
	}
	
	private void checkContentOut() throws IOException {
		if (this.content == null) {
			this.content = this.tempFileManager.createTempFile();
		}
		if (this.contentOut == null) {
			this.contentOut = new OutputStreamWriter(new FileOutputStream(this.content),"UTF-8");
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.paxle.parser.IParserDocument#addText(java.lang.CharSequence)
	 * @deprecated
	 */
	@Deprecated
	@Override
	public void addText(CharSequence text) throws IOException {
		checkContentOut();
		this.contentOut.write(text.toString());
	}
	
	@Override
	public Appendable append(char c) throws IOException {
		checkContentOut();
		this.contentOut.append(c);
		return this;
	}
	
	@Override
	public Appendable append(CharSequence csq) throws IOException {
		checkContentOut();
		this.contentOut.append(csq);
		return this;
	}
	
	@Override
	public Appendable append(CharSequence csq, int start, int end) throws IOException {
		checkContentOut();
		this.contentOut.append(csq, start, end);
		return this;
	}
	
	@Override
	public void setTextFile(File file) throws IOException {
		this.content = file;
	}
		
	@Override
	public File getTextFile() throws IOException {
		close();
		return this.content;
	}	
	
	@Override
	public Reader getTextAsReader() throws IOException {
		close();
		return (this.content == null) ? null : new InputStreamReader(new FileInputStream(this.content),Charset.forName("UTF-8"));
	}
	
	@Override
	public void close() throws IOException {
		if (this.contentOut != null) {
			this.contentOut.close();
		}
	}	
}
