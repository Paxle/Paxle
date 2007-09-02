package org.paxle.desktop.backend.impl.jre6;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.paxle.desktop.backend.desktop.IDesktop;

public class Desktop implements IDesktop {
	
	private static java.awt.Desktop desktop = null;
	
	public Desktop() {
		desktop = java.awt.Desktop.getDesktop();
	}
	
	public boolean browse(String url) throws MalformedURLException {
		return browse(new URL(url));
	}
	
	public boolean browse(URL url) {
		try {
			desktop.browse(url.toURI());
		} catch (URISyntaxException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
