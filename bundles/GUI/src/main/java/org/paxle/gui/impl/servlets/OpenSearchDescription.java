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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;

/**
 * @scr.component immediate="true" metatype="false"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="org.paxle.servlet.path" value="/opensearch/osd.xml"
 * @scr.property name="org.paxle.servlet.doUserAuth" value="false" type="Boolean"
 */
public class OpenSearchDescription extends ALayoutServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected Template getTemplate(HttpServletRequest request, HttpServletResponse response) {
		return this.getTemplate("/resources/opensearch/OpenSearchDescription.vm");
	}
	
	@Override
	protected void fillContext(Context context, HttpServletRequest request) {
		context.put("layout", "plain.vm");
	}

	@Override
	protected void setContentType(HttpServletRequest request, HttpServletResponse response) {
		if (request.getParameter("format") == null) {
			response.setContentType("application/opensearchdescription+xml");
		} else if (request.getParameter("format").equals("plain")) {
			response.setContentType("text/plain");
		}
	}
}
