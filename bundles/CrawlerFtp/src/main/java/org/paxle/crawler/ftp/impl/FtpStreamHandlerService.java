package org.paxle.crawler.ftp.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.net.ftp.FTP;
import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

public class FtpStreamHandlerService extends AbstractURLStreamHandlerService implements URLStreamHandlerService {
	public static final String PROTOCOL = "ftp";

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		return new FtpUrlConnection(url);
	}

	@Override
	public int getDefaultPort() {
		return FTP.DEFAULT_PORT;
	}
}
