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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;

import javax.annotation.CheckForNull;

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
	
	@CheckForNull
	public File getContent();
	public void setContent(File content);
	
	@CheckForNull
	public String[] getLanguages();	
	public void setLanguages(String[] lang);
	
	public Date getCrawlerDate();
	public void setCrawlerDate(Date crawlerDate);
	
	public Date getLastModDate();
	public void setLastModDate(Date lastModDate);
	
	@CheckForNull
	public byte[] getMD5Sum();
	public void setMD5Sum(byte[] md5Sum);
	
	public void close() throws IOException;
}
