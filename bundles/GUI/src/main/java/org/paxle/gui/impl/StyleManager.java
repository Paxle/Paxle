package org.paxle.gui.impl;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;

public class StyleManager
{
	
	private static Log logger = LogFactory.getLog( StyleManager.class);
	
	/** Vector containing available styles */
	private static HashMap<String, File> styles = new HashMap<String, File>();

	static {
		styles.put( "Red Style", new File("/home/markus/styletest.jar"));
	}
	
	
	public static HashMap<String, File> getStyles()
	{
		return styles;
	}
	
	
	public static void searchForStyles()
	{
		return;
	}
	
	
	public static void setStyle( String name)
	{
		
		ServletManager servletManager = Activator.getServletManager();
		
		HttpContext httpContextStyle = new HttpContextStyle( name);
		
		try {

			JarFile styleJarFile = new JarFile( name);
			
			Enumeration<?> jarEntryEnum = styleJarFile.entries(); 
			
			while( jarEntryEnum.hasMoreElements()) {
				
				JarEntry entry = (JarEntry) jarEntryEnum.nextElement();
				
				if( entry.isDirectory()) {
					
					String alias = "/" + entry.getName().substring( 0, entry.getName().length() - 1);
					
					logger.warn( "JarEntryName: " + alias);
					
					servletManager.removeResource( alias);
					
					servletManager.addResources( alias, alias , httpContextStyle);
				
				}
				
			}
		

		} catch (IOException e) {
			
			logger.error( "io: " + e);
			
			e.printStackTrace();
			
		}
		return;
	}


}


