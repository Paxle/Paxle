package org.paxle.core.doc;

import java.io.File;
import java.util.Date;



public class CrawlerDocument implements ICrawlerDocument {
	/**
	 * Primary key required by Object-EER mapping 
	 */
	private int _oid;
	
	private ICrawlerDocument.Status status = null;
	private String statusText = null;
	private String location = null;
	private String mimeType = null;
	private String charset = null;
	private String[] languages = null; 
	private File content = null;
	private Date crawlerDate = null;
	private Date lastModDate = null;
	private String etag;

    public int getOID(){ 
    	return _oid; 
    }

    public void setOID(int OID){ 
    	this._oid = OID; 
    }		
    
	public void setLocation(String location) {
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

	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	public String getEtag() {
		return this.etag;
	}
}
