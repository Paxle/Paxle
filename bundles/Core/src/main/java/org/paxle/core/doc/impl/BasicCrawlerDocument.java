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
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.paxle.core.doc.ICrawlerDocument;
import org.paxle.core.doc.impl.jaxb.JaxbFactory;
import org.paxle.core.doc.impl.jaxb.JaxbFileAdapter;

@XmlRootElement(name="crawlerDocument")
@XmlType(factoryClass=JaxbFactory.class, factoryMethod="createBasicCrawlerDocument")
public class BasicCrawlerDocument implements ICrawlerDocument {
	/**
	 * Primary key required by Object-EER mapping 
	 */
	protected int _oid;
	
	protected ICrawlerDocument.Status status = null;
	protected String statusText = null;
	/** The location, i.e. URL of this document */
	protected URI location = null;
	protected String mimeType = null;
	protected String charset = null;
	protected String[] languages = null; 
	protected File content = null;
	protected Date crawlerDate = null;
	protected Date lastModDate = null;
	protected String etag;
	protected byte[] md5Sum = null;

	@XmlAttribute(name="id")
    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }		
    
	public void setLocation(URI location) {
		this.location = location;
	}

	@XmlElement
	public URI getLocation() {
		return this.location;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	@XmlElement
	public String getCharset() {
		return this.charset;
	}

	@XmlTransient
	public long getSize() {
		return (this.content == null) ? 0 : this.content.length();
	}
	
	public void setContent(File content) {
		this.content = content;
	}

	@XmlElement
	@XmlJavaTypeAdapter(JaxbFileAdapter.class)
//	@XmlTransient
	public File getContent() {
		return this.content;
	}
	
//	@XmlAttachmentRef
//	public DataHandler getContentHandler() {
//		return new DataHandler(new FileDataSource(this.content));
//	}
//	
//	public void setContentHandler(DataHandler content) {
//		System.out.println("HIER");
//	}	
		
	public void setFileURI(String fileRef) {
		if (fileRef != null) this.content = new File(fileRef);
	}
	
	@XmlTransient
	public String getFileURI() {
		return (this.content == null)?null:this.content.toURI().toString();
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	@XmlElement
	public String getMimeType() {
		return this.mimeType;
	}
	
	@XmlElement
	public ICrawlerDocument.Status getStatus() {
		return this.status;
	}

	public void setStatus(ICrawlerDocument.Status status) {
		this.status = status;
	}
	
	@XmlElement
	public String getStatusText() {
		return this.statusText;
	}
	
	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}
	
	public void setStatus(ICrawlerDocument.Status status, String statusText) {
		this.setStatus(status);
		this.setStatusText(statusText);
	}
	
	public void setLanguages(String[] languages) {
		this.languages = languages;
	}
	
	@XmlElement(name="language")
	@XmlElementWrapper(name="languages")	
	public String[] getLanguages() {
		return this.languages;
	}

	public void setCrawlerDate(Date crawlerDate) {
		this.crawlerDate = crawlerDate;
	}
	
	@XmlElement
	public Date getCrawlerDate() {
		return this.crawlerDate;
	}

	public void setLastModDate(Date lastModDate) {
		this.lastModDate = lastModDate;
	}
	
	@XmlElement
	public Date getLastModDate() {
		return this.lastModDate;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	@XmlElement
	public String getEtag() {
		return this.etag;
	}
	
	@XmlElement
	public byte[] getMD5Sum() {
		return this.md5Sum;
	}
	
	public void setMD5Sum(byte[] md5Sum) {
		this.md5Sum = md5Sum;
	}
	
	public void close() throws IOException {
		if (this.content != null) {
			// TODO: return this.content to the ITempFileManager
		}
	}
}
