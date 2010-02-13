/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package org.paxle.gui.openid.impl;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.ParameterList;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.Authorization;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;

public class VerifyServlet extends AbstractControllerServlet {
	private static final long serialVersionUID = 1L;
	
	private final ConsumerManager manager;
	private final ServiceTracker userAdminTracker;
	private final Log logger = LogFactory.getLog(this.getClass());
	
	public VerifyServlet(final ServiceTracker userAdminTracker, final ConsumerManager manager) {
		this.userAdminTracker = userAdminTracker;
		this.manager = manager;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {	
		this.doPost(req,res);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			ParameterList openidResp = new ParameterList(request.getParameterMap());

			// get stored discoveryInfomation object
			HttpSession session = request.getSession(true);
			DiscoveryInformation discovered = (DiscoveryInformation) session.getAttribute("openid.discovered");

			// receivingURL
			StringBuffer receivingURL = request.getRequestURL();
			String queryString = request.getQueryString();
			if (queryString != null && queryString.length() > 0){
				receivingURL.append("?").append(request.getQueryString());
			}

			VerificationResult verification = this.manager.verify(receivingURL.toString(), openidResp, discovered);
			Identifier verified = verification.getVerifiedId();
			
			User user = null;
			UserAdmin userAdmin = (UserAdmin) this.userAdminTracker.getService();
			if (verified != null){
				System.out.println("success");				
				System.out.println(verified.getIdentifier());

				// authenticate user
				user = this.authenticatedAs(userAdmin, verified.getIdentifier());
			} 
			
			if (user != null){
				// remember login state
				session.setAttribute("logon.isDone", Boolean.TRUE);
				
				// set user-data into the session
				session.setAttribute(HttpContext.AUTHENTICATION_TYPE, "OPEN-ID");
				session.setAttribute(HttpContext.AUTHORIZATION, userAdmin.getAuthorization(user));
				session.setAttribute(HttpContext.REMOTE_USER, user);
				
				if (session.getAttribute("login.target") != null) {
					response.sendRedirect((String) session.getAttribute("login.target"));
				} else {
					response.sendRedirect("/");
				}
			} else {
//				forwardURL(req, res, "failed.jsp");
				System.out.println("error");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public User authenticatedAs(final UserAdmin userAdmin, final String openIdURL) {
		if (userAdmin == null) {
			logger.info("OSGi UserAdmin service not found");
			return null;
		}

		User user = userAdmin.getUser("openid.url",openIdURL);
		if( user == null ) {
			logger.info(String.format("No user found for OpenID-URL '%s'.", openIdURL));	
			return null;
		}

		Authorization authorization = userAdmin.getAuthorization(user);
		if(authorization == null) {
			logger.info(String.format("No authorization found for user with OpenID-URL '%s'.", openIdURL));
			return null;
		}

		return user;		
	}		
}