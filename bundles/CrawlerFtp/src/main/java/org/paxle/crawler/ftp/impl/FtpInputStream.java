package org.paxle.crawler.ftp.impl;

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FtpInputStream extends BufferedInputStream {
	private FtpUrlConnection ftpConnection = null;

	protected FtpInputStream(InputStream in, FtpUrlConnection ftpConnection) {
		super(in);
		this.ftpConnection = ftpConnection;
	}

	/**
	 * @see FilterInputStream
	 */
	@Override
	public int read() throws IOException {
		int b = super.read();
		if (b == -1) this.ftpConnection.closeConnection();
		return b;
	}
	
	/**
	 * @see FilterInputStream
	 */	
	@Override
	public int read(byte b[]) throws IOException {
		return this.read(b, 0, b.length);
	}	

	/**
	 * @see FilterInputStream
	 */	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = super.read(b, off, len);
		if (read == -1)  this.ftpConnection.closeConnection();
		return read;
	}

	/**
	 * @see FilterInputStream
	 */	
	@Override
	public void close() throws IOException {	
		super.close();
		this.ftpConnection.closeConnection();
	}
}
