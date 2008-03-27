package org.paxle.gui.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.JarFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;

public class HttpContextStyle implements HttpContext {

	/** for logging */
	private Log logger = LogFactory.getLog( this.getClass());

	/**
	 * The currently active style file
	 */
	private File styleFile;
	
	private JarFile styleJarFile;

	/**
	 * @param styleFile the file containing the currently configured layout
	 * @throws IOException
	 */
	public HttpContextStyle(File styleFile) throws IOException {
		this.styleFile = styleFile;
		this.styleJarFile = new JarFile(styleFile);
	}

	/**
	 * @see HttpContext#getMimeType(String)
	 */
	public String getMimeType( String name) {
		if (name.endsWith(".css")) return "text/css";
		return null;
	}

	/**
	 * @see HttpContext#getResource(String)
	 */
	public URL getResource(String name) {
		try {
			// checking if the requested file exists in the jar
			String tempName = name.startsWith("/") ? name.substring(1,name.length()) : name;
			if (this.styleJarFile.getJarEntry(tempName) == null) {
				this.logger.warn(String.format(
						"Resource '%s' not found in style-file '%s'.",
						name,
						this.styleFile.toString()
				));
				return null;
			}
			
			// building a url to load the resource
			String jarFilePath = this.styleFile.getCanonicalFile().toURI().toURL().toString();
			return new URL("jar:" + jarFilePath + "!" + name);
		} catch (Exception e) {			
			logger.error(String.format(
					"Unexpected '%s' while trying to load resource '%s' from style-file '%s'.",
					e.getClass().getName(),
					name,
					this.styleFile.toString()
			));
			return null;
		}
	}

	public boolean handleSecurity( HttpServletRequest request, HttpServletResponse response) throws IOException {
		return true;
	}

}
