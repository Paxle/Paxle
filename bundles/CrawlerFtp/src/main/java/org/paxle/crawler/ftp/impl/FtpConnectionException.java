package org.paxle.crawler.ftp.impl;

import java.io.IOException;

public class FtpConnectionException extends IOException {
	public FtpConnectionException(String msg) {
		super(msg);
	}
}
