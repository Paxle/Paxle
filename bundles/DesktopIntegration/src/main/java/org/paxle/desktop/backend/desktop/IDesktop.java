package org.paxle.desktop.backend.desktop;

import java.net.MalformedURLException;
import java.net.URL;

public interface IDesktop {
	
	public boolean browse(String url) throws MalformedURLException;
	public boolean browse(URL url);
}
