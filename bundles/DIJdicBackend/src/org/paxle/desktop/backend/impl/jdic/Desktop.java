package org.paxle.desktop.backend.impl.jdic;

import java.net.MalformedURLException;
import java.net.URL;

import org.jdesktop.jdic.desktop.DesktopException;
import org.paxle.desktop.backend.desktop.IDesktop;

public class Desktop implements IDesktop {
	
	public boolean browse(String url) throws MalformedURLException {
		return browse(new URL(url));
	}
	
	public boolean browse(URL url) {
		try {
			org.jdesktop.jdic.desktop.Desktop.browse(url);
		} catch (DesktopException e) {
			return false;
		}
		return true;
	}
}
