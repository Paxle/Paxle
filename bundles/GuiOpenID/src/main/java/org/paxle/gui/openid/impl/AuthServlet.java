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