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
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.consumer.ConsumerException;
import org.openid4java.consumer.ConsumerManager;
import org.openid4java.discovery.DiscoveryException;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.MessageException;

public class AuthServlet extends AbstractControllerServlet {
	private static final long serialVersionUID = 1L;
	
	private final ConsumerManager manager;
	
	public AuthServlet(ConsumerManager manager) {
		this.manager = manager;
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {	
		doPost(req, res);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			// openid_identifier
			String userSuppliedOpenID = request.getParameter("login.openid");

			// Normalization & Discovery
			List discoveries = null;
			try{
				discoveries = manager.discover(userSuppliedOpenID);
			}catch(DiscoveryException e){
				e.printStackTrace();
			}
			if(discoveries!=null){
				// associate
				DiscoveryInformation discovered = manager.associate(discoveries);
				HttpSession session = request.getSession(true);
				session.setAttribute("openid.discovered", discovered);
				String returnURL = String.format(
						"%s://%s:%d/openid/verify",
						request.getScheme(),
						request.getServerName(),
						request.getServerPort()
				);
					
				AuthRequest authReq = null;
				try{
					authReq = manager.authenticate(discovered, returnURL);
				}catch(ConsumerException e){
					e.printStackTrace();
				}catch(MessageException e){
					e.printStackTrace();
				}
				// authentication
				if(authReq!=null)response.sendRedirect(authReq.getDestinationUrl(true));
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}