package org.paxle.core.doc;

import java.io.File;
import java.util.Date;


public interface ICrawlerDocument {
	public static enum Status {
		OK,
		NOT_FOUND,
		UNKNOWN_FAILURE
	}

	public Status getStatus();
	public void setStatus(Status status);
	
	public String getLocation();
	public void setLocation(String location);
	
	public String getCharset();
	public void setCharset(String charset);
	
	public String getMimeType();
	public void setMimeType(String mimeType);
	
	public long getSize();
	
	public File getContent();
	public void setContent(File content);
	
	public String[] getLanguages();	
	
	public Date getCrawlerDate();
	public void setCrawlerDate(Date crawlerDate);
	
	public Date getLastModDate();
	public void setLastModDate(Date lastModDate);
}
