package org.paxle.gui.impl.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.osgi.service.useradmin.UserAdmin;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.HttpContextAuth;
import org.paxle.gui.impl.ServiceManager;

public class LoginView extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context)
	{
        Template template = null;

        try {
    		// Get the session
    		HttpSession session = request.getSession(true);
        	
        	if (request.getParameter("doLogin") != null) {
        		// getting username + password
        		String userName = request.getParameter("login.username");
        		String password = request.getParameter("login.password");
        		
        		// getting the userAdmin service
        		ServiceManager manager = (ServiceManager) context.get("manager");
        		UserAdmin uAdmin = (UserAdmin) manager.getService(UserAdmin.class.getName());
        		
        		if (HttpContextAuth.authenticated(uAdmin, request, userName, password)) {
        			// remember login state
        			session.setAttribute("logon.isDone", Boolean.TRUE);
        			if (session.getAttribute("login.target") != null) {
        				response.sendRedirect((String) session.getAttribute("login.target"));
        			}
        		} else {
        			context.put("errorMsg","Unable to login. Username or password is invalid");
        		}
        		
        	} else if (request.getParameter("doLogout") != null) {
        		session.removeAttribute("logon.isDone");
        	}

            
            template = this.getTemplate("/resources/templates/LoginView.vm");
        } catch( Exception e ) {
          System.err.println("Exception caught: " + e.getMessage());
        } catch (Error e) {
        	e.printStackTrace();
        }

        return template;
	}


}
