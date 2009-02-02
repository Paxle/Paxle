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
package org.paxle.core.doc;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

public class CrawlerDocument implements ICrawlerDocument {
	/**
	 * Primary key required by Object-EER mapping 
	 */
	private int _oid;
	
	private ICrawlerDocument.Status status = null;
	private String statusText = null;
	/** The location, i.e. URL of this document */
	private URI location = null;
	private String mimeType = null;
	private String charset = null;
	private String[] languages = null; 
	private File content = null;
	private Date crawlerDate = null;
	private Date lastModDate = null;
	private String etag;
	private byte[] md5Sum = null;

    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }		
    
	public void setLocation(URI location) {
		this.location = location;
	}

	public URI getLocation() {
		return this.location;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	public String getCharset() {
		return this.charset;
	}

	public long getSize() {
		return (this.content == null) ? 0 : this.content.length();
	}
	
	public void setContent(File content) {
		this.content = content;
	}

	public File getContent() {
		return this.content;
	}
	
	public void setFileURI(String fileRef) {
		if (fileRef != null) this.content = new File(fileRef);
	}
	
	public String getFileURI() {
		return (this.content == null)?null:this.content.toURI().toString();
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getMimeType() {
		return this.mimeType;
	}
	
	public ICrawlerDocument.Status getStatus() {
		return this.status;
	}

	public void setStatus(ICrawlerDocument.Status status) {
		this.status = status;
	}
	
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
	
	public String[] getLanguages() {
		return this.languages;
	}

	public void setCrawlerDate(Date crawlerDate) {
		this.crawlerDate = crawlerDate;
	}
	
	public Date getCrawlerDate() {
		return this.crawlerDate;
	}

	public void setLastModDate(Date lastModDate) {
		this.lastModDate = lastModDate;
	}
	
	public Date getLastModDate() {
		return this.lastModDate;
	}

	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	public String getEtag() {
		return this.etag;
	}
	
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
