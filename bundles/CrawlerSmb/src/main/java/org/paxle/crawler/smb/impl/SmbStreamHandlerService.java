package org.paxle.crawler.smb.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import jcifs.smb.SmbFile;

import org.osgi.service.url.AbstractURLStreamHandlerService;
import org.osgi.service.url.URLStreamHandlerService;

public class SmbStreamHandlerService extends AbstractURLStreamHandlerService implements URLStreamHandlerService  {
	public static final String PROTOCOL = "smb";

	@Override
	public URLConnection openConnection(URL url) throws IOException {
		return new SmbFile(url);
	}

	@Override
	public int getDefaultPort() {
		return 445;
	}
}
