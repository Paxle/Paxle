package org.paxle.gui.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;
import org.paxle.gui.IStyleManager;


public class StyleManager implements IStyleManager {

	/** 
	 * for logging
	 */
	private Log logger = LogFactory.getLog( StyleManager.class);

	/**
	 * A manager to manage http servlets and resources.
	 */
	private ServletManager servletManager = null;
	
	/**
	 * Path where all downloaded or installed styles are located
	 */
	private File dataPath = null;

	/** HashMap containing available styles */
	private HashMap<String, File> styles = new HashMap<String, File>();	

	public StyleManager(File dataPath, ServletManager servletManager) {
		if (dataPath == null) throw new NullPointerException("The datapath is null");
		if (servletManager == null) throw new NullPointerException("ServletManager is null");
		
		if (!dataPath.exists()) dataPath.mkdirs();
		this.dataPath = dataPath;
		this.servletManager = servletManager;
		
		// search for available styles
		this.searchForStyles();
	}

	public Collection<String> getStyles() {
		return Collections.unmodifiableCollection(styles.keySet());
	}

	public void searchForStyles() {
		// create temp map to remember found styles
		HashMap<String, File> temp = new HashMap<String, File>();

		// do a directory listing fo find all files
		File[] files = this.dataPath.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				temp.put( files[i].getName(), files[i]);
			}
		}

		this.styles = temp;
	}


	public void setStyle(String name) {
		if( name.equals( "default")) {

			this.servletManager.unregisterAllResources();			
			this.servletManager.addResources("/css","/resources/templates/layout/css");
			this.servletManager.addResources("/js","/resources/js");			
			this.servletManager.addResources("/images", "/resources/images");

			return;
		}
		
		try {
			File styleFile = new File(this.dataPath,name);
			HttpContext httpContextStyle = new HttpContextStyle(styleFile);
			
			JarFile styleJarFile = new JarFile(styleFile);
			Enumeration<?> jarEntryEnum = styleJarFile.entries();

			while (jarEntryEnum.hasMoreElements()) {
				JarEntry entry = (JarEntry) jarEntryEnum.nextElement();
				if (entry.isDirectory()) {
					String alias = "/" + entry.getName().substring( 0, entry.getName().length() - 1);
					servletManager.removeResource( alias);
					servletManager.addResources( alias, alias, httpContextStyle);
				}
			}
		} catch (IOException e) {
			logger.error( "io: " + e);
			e.printStackTrace();
		}
		return;
	}

}
