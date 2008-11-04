/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.paxle.gui.ALayoutServlet;

public class RootView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doRequest(HttpServletRequest request, HttpServletResponse response) {
		try {  	
			// just a redirection to the search view
			// TODO: path prefix handling is needed here
			response.sendRedirect("/search");
		} catch (Throwable e) {
			this.logger.error(e);
		}			
	}
}
