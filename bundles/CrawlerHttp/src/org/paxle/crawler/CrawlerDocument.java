package org.paxle.crawler;

import java.io.File;
import java.util.Date;

import org.paxle.core.doc.ICrawlerDocument;

public class CrawlerDocument implements ICrawlerDocument {
	private ICrawlerDocument.Status status = null;
	private String location = null;
	private String mimeType = null;
	private String charset = null;
	private String[] languages = null; 
	private File content = null;
	private Date crawlerDate = null;
	private Date lastModDate = null;
	private String etag;
		
	void setLocation(String location) {
		this.location = location;
	}

	public String getLocation() {
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

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	
	public String getMimeType() {
		return this.mimeType;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	public void setLanguages(String[] languages) {
		this.languages = languages;
	}
	
	public String[] getLanguages() {
		return this.languages;
	}

	public void setCrawlerDate(Date crawlerDate) {
		this.crawlerDate  = crawlerDate;
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

	public void setLanguages(String etag) {
		this.etag = etag;
	}
	
	public String getEtag() {
		return this.etag;
	}
}
