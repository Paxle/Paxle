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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.paxle.core.doc.impl.jaxb.JaxbFactory;
import org.paxle.core.doc.impl.jaxb.JaxbFileAdapter;
import org.paxle.core.io.temp.ITempFileManager;

@XmlRootElement(namespace="basic",name="parserDocument")
@XmlType(factoryClass=JaxbFactory.class, factoryMethod="createBasicParserDocument")
public class BasicParserDocument extends AParserDocument {
	
	public BasicParserDocument(ITempFileManager tempFileManager) {
		super(tempFileManager);
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
		
		// init internal structure
		this.contentWriter = null;		
		this.contentFile = file;
	}
			
	@XmlElement(name="text")
	@XmlJavaTypeAdapter(JaxbFileAdapter.class)
	@Override
	public File getTextFile() throws IOException {
		this.close();
		
		if (this.contentFile == null || !this.contentFile.exists() || this.contentFile.length() == 0) return null;
		return this.contentFile;
	}	
	
	@Override
	public Reader getTextAsReader() throws IOException {
		this.close();
		
		if (this.contentFile == null || !this.contentFile.exists() || this.contentFile.length() == 0) return null;
		return new InputStreamReader(new FileInputStream(this.contentFile),Charset.forName("UTF-8"));
	}
	
	public @Nonnull Writer getTextWriter() throws IOException {
		if (this.contentFile == null) {
			this.contentFile = this.tempFileManager.createTempFile();
		}
		if (this.contentFile != null && this.contentWriter == null) {
			this.contentWriter = new DocumentWriter(new OutputStreamWriter(new FileOutputStream(this.contentFile,true),"UTF-8"));
		}
		return this.contentWriter;
	}
	
	public long length() throws IOException {
		// flush data
		if (!this.closed && this.contentWriter != null) {
			this.contentWriter.flush();
		}
		
		if (this.contentFile == null || !this.contentFile.exists()) return 0;
		return this.contentFile.length();
	}
}
