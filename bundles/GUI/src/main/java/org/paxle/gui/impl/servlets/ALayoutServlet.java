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

package org.paxle.gui.impl.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.VelocityLayoutServlet;
import org.paxle.gui.IServiceManager;
import org.paxle.gui.IServletManager;

/**
 * @deprecated to not inherit from this class anymore. Use {@link VelocityLayoutServlet} instead
 */
public abstract class ALayoutServlet extends VelocityLayoutServlet {
    private static final long serialVersionUID = 1L;
    
    /**
     * Logger
     */
    protected Log logger = LogFactory.getLog(this.getClass());	
	
	@Override
    protected void fillContext(Context context, HttpServletRequest request) {
        // this implementation does nothing
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
				final IServiceManager manager = (IServiceManager) velocityContext.get(IServiceManager.SERVICE_MANAGER);				
				final IServletManager servletManager = (IServletManager) manager.getService(IServletManager.class.getName());				    			
				response.sendRedirect(servletManager.getFullServletPath("org.paxle.gui.impl.servlets.LoginView"));				
			}
			return false;
		}		
		return true;
	}
}
