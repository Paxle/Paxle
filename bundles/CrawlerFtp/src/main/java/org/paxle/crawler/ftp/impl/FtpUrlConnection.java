package org.paxle.crawler.ftp.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpUrlConnection extends URLConnection {
	private static final String DIR_MIMETYPE = "text/html";
	
	private Log logger = LogFactory.getLog(this.getClass());
	private FTPClient client = null;
	private int reply;
	private boolean isDirectory = false;
	private String file = "", path = null;

	protected FtpUrlConnection(URL url) {
		super(url);
		this.client = new FTPClient();
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
		String longpath = url.getPath();

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

	@Override
	public InputStream getInputStream() throws IOException {
		if (!this.client.isConnected()) this.connect();

		if (this.isDirectory) {
			StringWriter writer = new StringWriter();
			
			// getting the base dir
			String baseURL = this.path;
			if (!baseURL.endsWith("/")) baseURL += "/";
			
			// getting the parent dir
			String parentDir = "/";
			if (baseURL.length() > 1) {
				parentDir = baseURL.substring(0,baseURL.length()-1);
				int idx = parentDir.lastIndexOf("/");
				parentDir = parentDir.substring(0,idx+1);
			}
			
			writer.append(String.format("<html><head><title>Index of %s</title></head><hr><table><tbody>\r\n",this.url));
			writer.append(String.format("<tr><td colspan=\"3\"><a href=\"%s\">Up to higher level directory</a></td></tr>\r\n",parentDir));

			// generate directory listing
			FTPFile[] files = client.listFiles(path);
			for (FTPFile nextFile : files) {
				writer.append(
						String.format(
								"<tr>" + 
									"<td><a href=\"%1$s\">%2$s</a></td>" + 
									"<td>%3$d Bytes</td>" +
									"<td>%4$tY-%4$tm-%4$td %4$tT</td>" +
								"</tr>\r\n",
								baseURL + nextFile.getName(),
								nextFile.getName(),
								nextFile.isDirectory()?0:nextFile.getSize(),
								nextFile.getTimestamp()
						)
				);
			}
			writer.append("</tbody></table><hr></body></html>");			
		
			return new FtpInputStream(new ByteArrayInputStream(writer.toString().getBytes("UTF-8")),this);		
		} else {
			// switching to binary file transfer
			client.setFileType(FTPClient.BINARY_FILE_TYPE); 
			reply = client.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply)) {
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
				String msg = String.format("FTP server '%s' file transfer failed. ReplyCode: %s",url.getHost(),client.getReplyString());
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
