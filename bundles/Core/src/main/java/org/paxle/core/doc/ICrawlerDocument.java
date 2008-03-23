package org.paxle.core.doc;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

public interface ICrawlerDocument extends Closeable {
	public static enum Status {
		OK,
		NOT_FOUND,
		UNKNOWN_FAILURE		
	}
	
    public int getOID(); 
    public void setOID(int OID); 

	public Status getStatus();
	public String getStatusText();
	public void setStatusText(String statusText);
	public void setStatus(Status status);
	public void setStatus(Status status, String statusText);
	
	public URI getLocation();
	public void setLocation(URI location);
	
	public String getCharset();
	public void setCharset(String charset);
	
	public String getMimeType();
	public void setMimeType(String mimeType);
	
	public long getSize();
	
	public File getContent();
	public void setContent(File content);
	
	public String[] getLanguages();	
	public void setLanguages(String[] lang);
	
	public Date getCrawlerDate();
	public void setCrawlerDate(Date crawlerDate);
	
	public Date getLastModDate();
	public void setLastModDate(Date lastModDate);
	
	public byte[] getMD5Sum();
	public void setMD5Sum(byte[] md5Sum);
	
	public void close() throws IOException;
}
