
package org.paxle.desktop.backend.impl.jdic;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.jdic.desktop.DesktopException;
import org.paxle.desktop.backend.desktop.IDesktop;

public class Desktop implements IDesktop {
	
	private final Log logger = LogFactory.getLog(Desktop.class);
	
	public boolean browse(String url) throws MalformedURLException {
		return browse(new URL(url));
	}
	
	public boolean browse(URL url) {
		try {
			org.jdesktop.jdic.desktop.Desktop.browse(url);
		} catch (DesktopException e) {
			if (logger.isDebugEnabled()) {
				logger.error("Backend error starting browser", e);
			} else {
				logger.error(e.getMessage());
			}
			return false;
		} catch (LinkageError e) {
			logger.error("Linkage error starting browser: " + e.getMessage());
			return false;
		}
		return true;
	}
}
