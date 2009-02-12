package org.paxle.api.json.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;

public class HttpContextAuth implements HttpContext {
	public static final String USER_HTTP_PASSWORD = "http.password";
	public static final String USER_HTTP_LOGIN = "http.login";	
	
	/**
	 * The user-admin service required for authentication
	 */
	private final UserAdmin userAdmin;
	
	/**
	 * For logging
	 */
	private static Log logger = LogFactory.getLog(HttpContextAuth.class);	
	
	public HttpContextAuth(UserAdmin userAdmin) {
		this.userAdmin = userAdmin;
	}
	
	public String getMimeType(String arg0) {
		return null;
	}

	public URL getResource(String arg0) {
		return null;
	}

	public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
		User user = this.authenticateUser(request);
		if (user != null) {
			
			/*
			 * Remember Login-Data
			 * according to the OSGi Spec. we need to set this.
			 */
			request.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
			request.setAttribute(HttpContext.AUTHORIZATION, this.userAdmin.getAuthorization(user));
			request.setAttribute(HttpContext.REMOTE_USER, user);
		} else {
			response.setHeader("WWW-Authenticate", "Basic realm=\"Json API\"");
			response.sendError(401, "Authentication required");
		}
		
		return user != null;
	}
	
	private User authenticateUser(HttpServletRequest request) throws UnsupportedEncodingException {
		/*
		 * check if there is any http-auth header
		 */
		String auth = request.getHeader("Authorization");
		if (auth == null || auth.length() <= "Basic ".length()) {
			return null;
		}

		/*
		 * base64 decode and get username + password
		 */
		byte[] authBytes = Base64.decodeBase64(auth.substring("Basic ".length()).getBytes("UTF-8"));
		auth = new String(authBytes,"UTF-8");		
		String[] authData = auth.split(":");
		if (authData.length == 0) {
			logger.info(String.format(
					"[%s] No user-authentication data found to access '%s'.", 
					request.getRemoteHost(), 
					request.getRequestURI()
			));
			return null;
		}
		
		String userName = authData[0];
		String password = authData.length==1?"":authData[1];

		/*
		 * Trying to find a user with the given user-name
		 */
		User user = userAdmin.getUser(USER_HTTP_LOGIN,userName);
		if( user == null ) {
			logger.info(String.format(
					"[%s] No user found for username '%s'.", 
					request.getRemoteHost(), 
					userName
			));	
			return null;
		}

		/*
		 * Trying to authenticate the user with the given password
		 */
		if(!user.hasCredential(USER_HTTP_PASSWORD, password)) {
			logger.info(String.format(
					"[%s] Wrong password for username '%s'.", 
					request.getRemoteHost(), 
					userName
			));
			return null;
		}

		/*
		 * Checking authorization 
		 */
		Authorization authorization = userAdmin.getAuthorization(user);
		if(authorization == null) {
			logger.info(String.format(
					"[%s] No authorization found for username '%s'.", 
					request.getRemoteHost(), 
					userName
			));
			return null;
		}		
		if (!authorization.hasRole("Administrators")) {
//			this.logger.warn(String.format(""))
		}		
		
		return user;
	}

}
