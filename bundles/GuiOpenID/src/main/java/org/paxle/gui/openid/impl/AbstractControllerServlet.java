package org.paxle.gui.openid.impl;

import java.io.IOException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class AbstractControllerServlet extends HttpServlet {
	public InitialContext ic = null;
	public final void init( ) throws ServletException {
		try {
			InitialContext ic = new InitialContext();
		} catch (NamingException e) {
			e.printStackTrace(); 
		} catch(Exception e) { 
			e.printStackTrace(); 
		}
		doInit();
	}
	
	public void doInit() throws ServletException {
	}
	
	public final void destroy() {
		doDestroy();
	}

	public void doDestroy() {
	}

	public final void forwardURL(HttpServletRequest req, HttpServletResponse res, String jsp)
			throws ServletException, IOException {
		RequestDispatcher rd = req.getRequestDispatcher(jsp);
		rd.forward(req, res);
	}
}