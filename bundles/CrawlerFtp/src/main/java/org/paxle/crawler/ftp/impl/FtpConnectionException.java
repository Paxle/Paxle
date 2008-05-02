package org.paxle.crawler.ftp.impl;

import java.io.IOException;

public class FtpConnectionException extends IOException {
	
	private static final long serialVersionUID = 1L;
	
	public FtpConnectionException(String msg) {
		super(msg);
	}
}
