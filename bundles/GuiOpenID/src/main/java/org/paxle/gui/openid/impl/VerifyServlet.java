package org.paxle.gui.openid.impl;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.openid4java.consumer.ConsumerManager;
import org.openid4java.consumer.VerificationResult;
import org.openid4java.discovery.DiscoveryInformation;
import org.openid4java.discovery.Identifier;
import org.openid4java.message.ParameterList;

public class VerifyServlet extends AbstractControllerServlet {
	private static final long serialVersionUID = 1L;
	
	private final ConsumerManager manager;
	
	public VerifyServlet(ConsumerManager manager) {
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

			VerificationResult verification = null;
			try{
				verification = this.manager.verify(receivingURL.toString(), openidResp, discovered);
			}catch(Exception e){
				e.printStackTrace();
			}
			Identifier verified = null;
			if(verification!=null){
				verified = verification.getVerifiedId();
			}
			if (verified != null){
				System.out.println("success");				
				System.out.println(verified.getIdentifier());
				
				// TODO: check if we have a user with this identifier via UserAdmin
				
    			// remember login state
    			session.setAttribute("logon.isDone", Boolean.TRUE);				
    			if (session.getAttribute("login.target") != null) {
    				response.sendRedirect((String) session.getAttribute("login.target"));
    			}
			}else{
//				forwardURL(req, res, "failed.jsp");
				System.out.println("error");
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}