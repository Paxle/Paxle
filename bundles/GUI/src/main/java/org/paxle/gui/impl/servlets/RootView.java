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
package org.paxle.gui.impl.servlets;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.IServletManager;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="false" type="Boolean"
 */
public class RootView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	private IServletManager smanager = null;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.smanager = (IServletManager) config.getServletContext().getAttribute("servletManager");
	}
	
	@Override
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		try {  	
			// just a redirection to the search view
			response.sendRedirect(smanager.getFullServletPath(SearchView.class.getName()));
		} catch (Throwable e) {
			this.logger.error(e);
		}			
	}
}
