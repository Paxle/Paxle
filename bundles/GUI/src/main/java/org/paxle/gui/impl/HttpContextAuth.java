package org.paxle.gui.impl;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;

/*
 * User authentication here. Please read
 * http://www2.osgi.org/javadoc/r4/index.html to see how this could
 * be used in combination with the OSGI User Admin Service
 * (http://www2.osgi.org/javadoc/r4/org/osgi/service/useradmin/package-summary.html)
 */
public class HttpContextAuth implements HttpContext
{
	
	private Log logger = LogFactory.getLog( this.getClass());
	
	private boolean loggedIn = false;

	private Bundle bundle;
	
	public HttpContextAuth( Bundle b)
	{
		bundle = b;
		
		return;
	}
	
	public String getMimeType( String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public URL getResource( String name)
	{
		return bundle.getResource( name);
	}

	public boolean handleSecurity( HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		if( loggedIn) {
			
			return true;
			
		}
		else {
			
			logger.info( "authentication needed");
			
			response.setStatus(401);
			
			response.setHeader("WWW-Authenticate", "Basic realm=\"paxle log-in\"");
			
			loggedIn = true; // FIXME
			
			return false;
			
		}
	}

}
