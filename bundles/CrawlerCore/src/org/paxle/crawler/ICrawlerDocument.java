package org.paxle.crawler;

import java.io.File;


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
}
