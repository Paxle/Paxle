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
package org.paxle.gui;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityLayoutServlet;
import org.apache.velocity.tools.view.VelocityView;

import edu.umd.cs.findbugs.annotations.OverrideMustInvoke;

public abstract class ALayoutServlet extends VelocityLayoutServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * A constant to fetch the service-manager from the velocity context
     */
    public static final String SERVICE_MANAGER = "manager";
	
    /**
     * Logger
     */
    protected Log logger = LogFactory.getLog(this.getClass());

	private VelocityView view;
	
	private IVelocityViewFactory viewFactory;
	
	@Override
	protected VelocityView getVelocityView() {
		return this.view;
	}
	
	public void setVelocityViewFactory(IVelocityViewFactory viewFactory) {
		this.viewFactory = viewFactory;
	}

	@Override
	@OverrideMustInvoke
	public void init(ServletConfig config) throws javax.servlet.ServletException {	
		this.view = this.viewFactory.createVelocityView(config);	
		super.init(config);
	};
	
	@Override
	public void destroy() {
		super.destroy();
		this.view = null;
		this.viewFactory = null;
	}
	
	@Override
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context) throws Exception {
		return super.handleRequest(request, response, context);
	}
	
	@Override
    protected void fillContext(Context context, HttpServletRequest request) {
        // this implementation does nothing
    }
	
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return super.getTemplate(request, response);
	}
	
	
	protected boolean isUserAuthenticated(final HttpServletRequest request, final HttpServletResponse response, boolean redirectToLogin) throws IOException {
		HttpSession session = request.getSession(true);
		Boolean loginDone = (Boolean) session.getAttribute("logon.isDone");
		if (loginDone == null || !Boolean.TRUE.equals(loginDone)) {
			if (redirectToLogin) {
				// storing original target into session
				session.setAttribute("login.target",request.getServletPath());
				
				// redirecting the browser
				final Context velocityContext = getVelocityView().createContext(request, response);
				final IServiceManager manager = (IServiceManager) velocityContext.get(SERVICE_MANAGER);				
				final IServletManager servletManager = (IServletManager) manager.getService(IServletManager.class.getName());				    			
				response.sendRedirect(servletManager.getFullServletPath("org.paxle.gui.impl.servlets.LoginView"));				
			}
			return false;
		}		
		return true;
	}
}
