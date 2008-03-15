package org.paxle.gui.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;

public class HttpContextStyle implements HttpContext
{
	
	private Log logger = LogFactory.getLog( this.getClass());
	
	private String jarFilePath;
	
	public HttpContextStyle( String path)
	{
		jarFilePath = path;
	}

	public String getMimeType( String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public URL getResource( String name)
	{
		URL url = null;
	
		try {
						
			url = new URL("jar:file://" + jarFilePath + "!" + name);
			
		} catch (MalformedURLException e) {
			
			logger.error( "url: " + e);
			
			e.printStackTrace();
			
		}
		
		return url; 
	}

	public boolean handleSecurity( HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		return true;
	}

}
