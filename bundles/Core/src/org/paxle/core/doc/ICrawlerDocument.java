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
	public String getLocation();
	public String getCharset();
	public String getMimeType();
	public long getSize();
	public File getContent();
	public String[] getLanguages();
	public Date getCrawlerDate();
	public Date getLastModDate();
}
