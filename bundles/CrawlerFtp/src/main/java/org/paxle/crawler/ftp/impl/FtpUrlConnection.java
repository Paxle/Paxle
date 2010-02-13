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

package org.paxle.crawler.ftp.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.Formatter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPListParseEngine;
import org.apache.commons.net.ftp.FTPReply;

public class FtpUrlConnection extends URLConnection {
	private static final String DIR_MIMETYPE = "text/html";
	
	private Log logger = LogFactory.getLog(this.getClass());
	private FTPClient client = null;
	private int reply;
	private boolean isDirectory = false;
	private String file = "", path = null;
	private final URI uri;

	protected FtpUrlConnection(URL url) throws URISyntaxException {
		super(url);
		this.uri = url.toURI();
		this.client = new FTPClient();
		this.setConnectTimeout(15000);
		this.setReadTimeout(15000);
	}

	@Override
	public void connect() throws IOException {
		if (this.connected) return;

		/* =======================================================================
		 * Connect to host
		 * ======================================================================= */
		this.client.connect(this.url.getHost(),(this.url.getPort() != -1)?this.url.getPort():FTP.DEFAULT_PORT);
		this.reply = this.client.getReplyCode();
		if(!FTPReply.isPositiveCompletion(this.reply)) {
			client.disconnect();
			String msg = String.format("FTP server '%s' refused connection. ReplyCode: %s",url.getHost(),client.getReplyString());
			this.logger.warn(msg);
			throw new FtpConnectionException(msg);
		} else {
			this.logger.debug(String.format("Connected to '%s'. ReplyCode: %s",url.getHost(),client.getReplyString()));
		}

		/* =======================================================================
		 * Login
		 * ======================================================================= */
		String userName = "anonymous", pwd = "anonymous";
		String userInfo = url.getUserInfo();
		if (userInfo != null) {
			int idx = userInfo.indexOf(":");
			if (idx != -1) {
				userName = userInfo.substring(0,idx).trim();
				pwd = userInfo.substring(idx+1).trim();
			}
		}
		client.login(userName, pwd);
		reply = client.getReplyCode();
		if(!FTPReply.isPositiveCompletion(reply)) {
			client.disconnect();
			String msg = String.format("FTP server '%s' login failed. ReplyCode: %s",url.getHost(),client.getReplyString());
			this.logger.warn(msg);
			throw new FtpConnectionException(msg);
		} 		

		/* =======================================================================
		 * Change directory
		 * ======================================================================= */			
		String longpath = uri.getPath();

		if (longpath.endsWith("/")) {
			file = "";
			path = longpath;
		} else {
			int pos = longpath.lastIndexOf("/");
			if (pos == -1) {
				file = longpath;
				path = "/";
			} else {            
				path = longpath.substring(0,pos+1);
				file = longpath.substring(pos+1);
			}
		}   

		client.changeWorkingDirectory(path);
		reply = client.getReplyCode();
		if(!FTPReply.isPositiveCompletion(reply)) {
			client.disconnect();
			String msg = String.format("FTP server '%s' change directory failed. ReplyCode: %s",url.getHost(),client.getReplyString());
			this.logger.warn(msg);
			throw new FtpConnectionException(msg);
		} 			

		// test if the specified file is also an directory
		if (file.length() > 0) {
			client.changeWorkingDirectory(file);
			reply = client.getReplyCode();
			if(FTPReply.isPositiveCompletion(reply)) {
				longpath += "/";
				path = longpath;
				file = "";
			}
		}		

		this.isDirectory = (file.length() == 0);
		this.connected = true;
	}
	
	public boolean isDirectory() throws IOException {
		if (!this.client.isConnected()) this.connect();
		return this.isDirectory;
	}
	
	public FTPFile[] listFiles() throws IOException {
		if (!this.client.isConnected()) this.connect();
		return client.listFiles();
	}
	
	private static final class BAOS extends ByteArrayOutputStream {
		public byte[] getBuffer() {
			return super.buf;
		}
	}
	
	private static final int[] FILE_ACCESS_MODES = { FTPFile.USER_ACCESS, FTPFile.GROUP_ACCESS, FTPFile.WORLD_ACCESS };
	
	/* Generates a listing like the following (this example is from ftp.debian.org/debian):
	 *
-rw-r--r--   1      1176      1176          1060  2009-04-11 17:05  README
-rw-r--r--   1      1176      1176          1290  2000-12-04 00:00  README.CD-manufacture
-rw-r--r--   1      1176      1176          2581  2009-04-11 17:06  README.html
-rw-r--r--   1      1176      1176        123286  2009-04-20 13:53  README.mirrors.html
-rw-r--r--   1      1176      1176         61886  2009-04-20 13:53  README.mirrors.txt
drwxr-xr-x  11      1176      1176          4096  2009-04-11 17:06  dists
drwxr-xr-x   4      1176      1176          4096  2009-04-22 19:53  doc
drwxr-xr-x   3      1176      1176          4096  2009-04-22 20:48  indices
-rw-r--r--   1      1176      1176       5207034  2009-04-22 20:48  ls-lR.gz
-rw-r--r--   1      1176      1176         91849  2009-04-22 20:48  ls-lR.patch.gz
drwxr-xr-x   5      1176      1176          4096  2000-12-19 00:00  pool
drwxr-xr-x   4      1176      1176          4096  2008-11-17 23:05  project
drwxr-xr-x   2      1176      1176          4096  2009-02-07 00:33  tools
	 */
	private static void formatStdDirlisting(final OutputStream into, final FTPListParseEngine fileParseEngine) {
		final Formatter writer = new Formatter(into);
		FTPFile[] files;
		while (fileParseEngine.hasNext()) {
			files = fileParseEngine.getNext(16);
			for (final FTPFile file : files) {
				if (file == null)
					continue;
				
				// directory
				char c;
				switch (file.getType()) {
					case FTPFile.DIRECTORY_TYPE:     c = 'd'; break;
					case FTPFile.SYMBOLIC_LINK_TYPE: c = 's'; break;
					default:                         c = '-'; break;
				}
				writer.format("%c", Character.valueOf(c));
				
				// permissions
				for (final int access : FILE_ACCESS_MODES) {
					writer.format("%c%c%c",
							Character.valueOf(file.hasPermission(access, FTPFile.READ_PERMISSION)    ? 'r' : '-'),
							Character.valueOf(file.hasPermission(access, FTPFile.WRITE_PERMISSION)   ? 'w' : '-'),
							Character.valueOf(file.hasPermission(access, FTPFile.EXECUTE_PERMISSION) ? 'x' : '-'));
				}
				
				// other information
				writer.format("  %2d", Integer.valueOf(file.getHardLinkCount()));
				writer.format("  %8s", file.getUser());
				writer.format("  %8s", file.getGroup());
				writer.format("  %12d", Long.valueOf(file.getSize()));
				writer.format("  %1$tY-%1$tm-%1$td %1$tH:%1$tM", Long.valueOf(file.getTimestamp().getTimeInMillis()));
				writer.format("  %s", file.getName());
				
				writer.format("%s", System.getProperty("line.separator"));
			}
		}
		
		writer.flush();
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (!this.client.isConnected()) this.connect();

		if (this.isDirectory) {
			final BAOS baos = new BAOS();
			
			final FTPListParseEngine fileParseEngine = client.initiateListParsing();
			formatStdDirlisting(baos, fileParseEngine);
			
			return new ByteArrayInputStream(baos.getBuffer(), 0, baos.size());
			
		} else {
			// switching to binary file transfer
			client.setFileType(FTPClient.BINARY_FILE_TYPE); 
			reply = client.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				client.disconnect();
				String msg = String.format("FTP server '%s' changing transfer mode failed. ReplyCode: %s",url.getHost(),client.getReplyString());
				this.logger.warn(msg);
				throw new FtpConnectionException(msg);
			} 				
			
			// copy file
			InputStream fileInputStream = client.retrieveFileStream(file);
			reply = client.getReplyCode();
			if (!FTPReply.isPositivePreliminary(reply) || fileInputStream == null) {
				client.disconnect();
				final String msg = String.format("FTP server '%s' file transfer failed. ReplyCode: %s (%d)",
						url.getHost(), client.getReplyString(), Integer.valueOf(reply));
				this.logger.warn(msg);
				throw new FtpConnectionException(msg);
			}

			return new FtpInputStream(fileInputStream,this);
		}
	}		

	@Override
	public int getContentLength() {
		if (!this.client.isConnected() || this.isDirectory) return -1;

		int size = -1;
		try {
			FTPFile file = this.findFile();
			if (file != null) {
				size = (int) file.getSize();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return size;
	}

	@Override
	public long getLastModified() {
		if (!this.client.isConnected()) return 0l;

		long lastModTimestamp = 0l;
		try {
			if (this.isDirectory) {
				Calendar lastMod = null;
				FTPFile[] files = client.listFiles(path);
				for (FTPFile nextFile : files) {
					Calendar fileDate = nextFile.getTimestamp();
					lastMod = (lastMod==null||fileDate.after(lastMod))?fileDate:lastMod;
				}		
				if (lastMod !=null) {
					lastModTimestamp = lastMod.getTimeInMillis();
				}
			} else {
				FTPFile file = this.findFile();
				if (file != null) {
					lastModTimestamp = file.getTimestamp().getTimeInMillis();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lastModTimestamp;
	}

	@Override
	public String getContentType() {
		if (!this.client.isConnected()) return null;
		if (this.isDirectory) return DIR_MIMETYPE;

		String contentType = null;
		try {
			InputStream inputStream = this.getInputStream();
			contentType = URLConnection.guessContentTypeFromStream(inputStream);
			inputStream.close();
			
			// we need to reconnect here
			if (!this.client.isConnected()) this.connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return contentType;
	}

	private FTPFile findFile() throws IOException {
		// get all files in the current directory
		FTPFile[] files = client.listFiles(path);

		// loop through the files to find our requested file
		for (FTPFile nextFile : files) {
			if (nextFile.getName().equals(this.file)) {
				return nextFile;
			}
		}
		return null;
	}	

	@Override
	public void setReadTimeout(int timeout) {
		super.setReadTimeout(timeout);
		this.client.setDataTimeout(timeout);
	}

	@Override
	public void setConnectTimeout(int timeout) {
		super.setConnectTimeout(timeout);
		this.client.setDefaultTimeout(timeout);
	}
		
	public void closeConnection() throws IOException {
		if (this.client.isConnected()) {
			this.client.logout();
			this.client.disconnect();
			this.connected = false;
		}
	}
}
