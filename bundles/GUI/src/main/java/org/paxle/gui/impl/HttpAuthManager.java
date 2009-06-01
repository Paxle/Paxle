/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
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
import org.paxle.gui.IServletManager;
import org.paxle.gui.impl.servlets.LoginView;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="org.paxle.gui.impl.IHttpAuthManager"
 */
public class HttpAuthManager implements IHttpAuthManager {
	public static final String USER_HTTP_PASSWORD = "http.password";
	public static final String USER_HTTP_LOGIN = "http.login";

	/**
	 * For logging
	 */
	private Log logger = LogFactory.getLog(HttpAuthManager.class);

	/**
	 * The OSGI {@link UserAdmin} service required for user athentication
	 * @scr.reference
	 */
	protected UserAdmin userAdmin;
	
	/**
	 * @scr.reference cardinality="0..1" policy="dynamic"
	 */
	protected IServletManager smanager;

	public User autoLogin(final HttpServletRequest request) {
		User user = null;
		
		final String[] autoLoginProperties = new String[] {
			"org.paxle.gui.autologin.user",
			"org.paxle.gui.autologin.user." + request.getRemoteHost(),
			"org.paxle.gui.autologin.user." + request.getRemoteAddr()
		};
		
		for (String autoLoginProperty : autoLoginProperties) {
			String autoLoginUser = System.getProperty(autoLoginProperty);
	 		if (autoLoginUser != null && autoLoginUser.length() > 0) {
	 			user = userAdmin.getUser(USER_HTTP_LOGIN,System.getProperty(autoLoginProperty));
	 			if (user != null) break;
	 		} 
		}
		
		return user;
	}
	
	public User httpAuth(final HttpServletRequest request, String httpAuthHeader) throws UnsupportedEncodingException {		
		if (httpAuthHeader == null || httpAuthHeader.length() <= "Basic ".length()) {
			return null;
		}

		// base64 decode and get username + password
		byte[] authBytes = Base64.decodeBase64(httpAuthHeader.substring("Basic ".length()).getBytes("UTF-8"));
		httpAuthHeader = new String(authBytes,"UTF-8");		
		String[] authData = httpAuthHeader.split(":");
		if (authData.length == 0) {
			logger.info(String.format("[%s] No user-authentication data found to access '%s'.", request.getRemoteHost(), request.getRequestURI()));
			return null;
		}

		// extracting username + password
		String userName = authData[0];
		String password = authData.length==1?"":authData[1];
		
		return authenticatedAs(request, userName, password);
	}
	
	public User authenticatedAs(final HttpServletRequest request, final String userName, String password) {
		final User user = userAdmin.getUser(USER_HTTP_LOGIN,userName);
		if( user == null ) {
			logger.info(String.format("[%s] No user found for username '%s'.", request.getRemoteHost(), userName));	
			return null;
		}

		if(!user.hasCredential(USER_HTTP_PASSWORD, password)) {
			logger.info(String.format("[%s] Wrong password for username '%s'.", request.getRemoteHost(), userName));
			return null;
		}

		Authorization authorization = userAdmin.getAuthorization(user);
		if(authorization == null) {
			logger.info(String.format("[%s] No authorization found for username '%s'.", request.getRemoteHost(), userName));
			return null;
		}
		
		if (!authorization.hasRole("Administrators")) {
//			this.logger.warn(String.format(""))
		}

		return user;		
	}	
	
	public HttpContext createHttpAuthContext(Bundle bundle) {
		return new HttpContextAuth(bundle);
	}
	
	/*
	 * User authentication here. Please read
	 * http://www2.osgi.org/javadoc/r4/index.html to see how this could
	 * be used in combination with the OSGI User Admin Service
	 * (http://www2.osgi.org/javadoc/r4/org/osgi/service/useradmin/package-summary.html)
	 */	
	private class HttpContextAuth implements HttpContext {
		private final Bundle bundle;
		
		public HttpContextAuth(Bundle bundle) {
			this.bundle = bundle;
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
				// auto login feature
				User user = autoLogin(request);
				
				// auth. user via http-auth header
		 		if (user == null) user = httpAuth(request, httpAuth);			
				if (user != null) {
					done = Boolean.TRUE;
					session.setAttribute("logon.isDone", done);
					
					// set user-data into the session
					session.setAttribute(HttpContext.AUTHENTICATION_TYPE, HttpServletRequest.BASIC_AUTH);
					session.setAttribute(HttpContext.AUTHORIZATION, userAdmin.getAuthorization(user));
					session.setAttribute(HttpContext.REMOTE_USER, user);				
				}
			}
			
			// if we are still not authenticated, try to use form-based login
			if (done == null) {
				StringBuilder target = new StringBuilder(request.getRequestURI());
				if (request.getQueryString() != null) {
					target.append("?")
						  .append(request.getQueryString());
				}
				
				// No logon.isDone means he hasn't logged in.
				// Save the request URL as the true target and redirect to the login page.
				// XXX: this currently just works for GET requests
				session.setAttribute("login.target", target.toString());
				response.sendRedirect(String.format(
						"%s://%s:%d%s",
						request.getScheme(),
						request.getServerName(),
						Integer.valueOf(request.getServerPort()),
						smanager.getFullServletPath(LoginView.class.getName())
				));
				return false;
			} 

			// according to the OSGi Spec. we need to set this.
			request.setAttribute(HttpContext.AUTHENTICATION_TYPE, session.getAttribute(HttpContext.AUTHENTICATION_TYPE));
			request.setAttribute(HttpContext.AUTHORIZATION, session.getAttribute(HttpContext.AUTHORIZATION));
			request.setAttribute(HttpContext.REMOTE_USER, session.getAttribute(HttpContext.REMOTE_USER));

			return true;
		}
		
	}
}
