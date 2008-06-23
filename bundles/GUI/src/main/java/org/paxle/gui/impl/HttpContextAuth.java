package org.paxle.gui.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;

/*
 * User authentication here. Please read
 * http://www2.osgi.org/javadoc/r4/index.html to see how this could
 * be used in combination with the OSGI User Admin Service
 * (http://www2.osgi.org/javadoc/r4/org/osgi/service/useradmin/package-summary.html)
 */
public class HttpContextAuth implements HttpContext {
	public static final String USER_HTTP_PASSWORD = "http.password";
	public static final String USER_HTTP_LOGIN = "http.login";

	/**
	 * For logging
	 */
	private static Log logger = LogFactory.getLog(HttpContextAuth.class);

	private final Bundle bundle;

	private final ServiceTracker uAdminTracker;

	public HttpContextAuth(Bundle b, ServiceTracker uAdminTracker) {
		this.bundle = b;
		this.uAdminTracker = uAdminTracker;
	}

	public String getMimeType( String name) {
		return null;
	}

	public URL getResource( String name) {
		return bundle.getResource( name);
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// check if there is any http-auth header
		String httpAuth = request.getHeader("Authorization");
		
		// Get the session
		HttpSession session = request.getSession(true);

		// check if we are already authenticated
		Object done = session.getAttribute("logon.isDone");  // marker object
		
		// determine if http-auth can be done
		if (done == null) {
			UserAdmin uAdmin = (UserAdmin) this.uAdminTracker.getService();
			
			if (httpAuth(uAdmin, request, httpAuth)) {
				done = Boolean.TRUE;
				session.setAttribute("logon.isDone", done);
			}
		}
		
		// if we are still not authenticated, try to use form-based login
		if (done == null) {			
			// No logon.isDone means he hasn't logged in.
			// Save the request URL as the true target and redirect to the login page.
			session.setAttribute("login.target", request.getRequestURL().toString());
			response.sendRedirect(String.format(
					"%s://%s:%d/login",
					request.getScheme(),
					request.getServerName(),
					request.getServerPort()
			));
			return false;
		}
		return true;

	}
	
	public static boolean httpAuth(final UserAdmin userAdmin, final HttpServletRequest request, String auth) throws UnsupportedEncodingException {
		if (auth.length() <= "Basic ".length()) {
			return false;
		}

		// base64 decode and get username + password
		byte[] authBytes = Base64.decodeBase64(auth.substring("Basic ".length()).getBytes("UTF-8"));
		auth = new String(authBytes,"UTF-8");		
		String[] authData = auth.split(":");
		if (authData.length == 0) {
			logger.info(String.format("[%s] No user-authentication data found to access '%s'.", request.getRemoteHost(), request.getRequestURI()));
			return false;
		}
		
		String userName = authData[0];
		String password = authData.length==1?"":authData[1];

		if (userAdmin == null) {
			logger.info(String.format("[%s] OSGi UserAdmin service not found", request.getRemoteHost()));
			return false;
		}
		
		return authenticated(userAdmin, request, userName, password);
	}
	
	public static boolean authenticated(final UserAdmin userAdmin, final HttpServletRequest request, final String userName, final String password) {
		if (userAdmin == null) {
			logger.info(String.format("[%s] OSGi UserAdmin service not found", request.getRemoteHost()));
			return false;
		}

		User user = userAdmin.getUser(USER_HTTP_LOGIN,userName);
		if( user == null ) {
			logger.info(String.format("[%s] No user found for username '%s'.", request.getRemoteHost(), userName));	
			return false;
		}

		if(!user.hasCredential(USER_HTTP_PASSWORD, password)) {
			logger.info(String.format("[%s] Wrong password for username '%s'.", request.getRemoteHost(), userName));
			return false;
		}

		Authorization authorization = userAdmin.getAuthorization(user);
		if(authorization == null) {
			logger.info(String.format("[%s] No authorization found for username '%s'.", request.getRemoteHost(), userName));
			return false;
		}
		
		if (!authorization.hasRole("Administrators")) {
//			this.logger.warn(String.format(""))
		}

		// according to the OSGi spec we need to set the following properties ...
		request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.FORM_AUTH);
		request.setAttribute(HttpContext.AUTHORIZATION, authorization);
		request.setAttribute(HttpContext.REMOTE_USER, user);

		return true;		
	}	
}
