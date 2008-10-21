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

package org.paxle.filter.blacklist.impl.gui;

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.filter.blacklist.impl.Blacklist;
import org.paxle.filter.blacklist.impl.BlacklistFilter;
import org.paxle.filter.blacklist.impl.InvalidFilenameException;
import org.paxle.gui.ALayoutServlet;

public class BlacklistServlet extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	private BlacklistFilter blacklistFilter = null;

	public BlacklistServlet(BlacklistFilter blacklistFilter) {
		this.blacklistFilter = blacklistFilter;
	}



	@Override
	public Template handleRequest( HttpServletRequest request,
			HttpServletResponse response,
			Context context ) {
		Template template = null;
		try {
			template = this.getTemplate("/resources/templates/Blacklist.vm");
			List<String> listnames = this.blacklistFilter.getLists();
			context.put("listnames", listnames);

			// get the action
			String action = request.getParameter("action");
			if (action == null)
				action = "list";
			context.put("action", action);

			// create a new list
			if (request.getMethod().equals("POST") && action.equals("addList")) {
				if ((request.getParameter("listName") != null) && (! listnames.contains(request.getParameter("listName")))) {
					try{
						blacklistFilter.createList(request.getParameter("listName"));
					} catch (InvalidFilenameException e) {
						logger.warn("Tried to add blacklist with invalid name '" + request.getParameter("listName") + "'");
						context.put("InvalidFilenameException", e);
						return template;
					}
					response.sendRedirect("/blacklist?list=" + URLEncoder.encode(request.getParameter("listName"), "UTF-8"));
					return null;
				}
			}

			// get the current list
			Blacklist blacklist = null;
			if (request.getParameter("list") != null)
				blacklist = blacklistFilter.getList(request.getParameter("list"));

			// check if this list exists:
			if (blacklist == null) {
				if (! listnames.isEmpty()) // get the first list when there is one
					blacklist = blacklistFilter.getList(listnames.get(0));
				else if (! action.equals("newList")) { // well, it seems there aren't any lists, so the user might want to create one, let's redirect him
					response.sendRedirect("/blacklist?action=newList");
					return null;
				}
			} else if (request.getMethod().equals("POST")) { // these are non-safe methods and should only be executed when there is a post-request and a valid list was given
				if (action.equals("removeList")) {
					blacklist.destroy();
					response.sendRedirect("/blacklist");
					return null;
				} else if (action.equals("addPattern") && request.getParameter("pattern") != null) {
					blacklist.addPattern(request.getParameter("pattern"));
					response.sendRedirect("/blacklist?list=" + URLEncoder.encode(blacklist.name, "UTF-8"));
					return null;
				} else if (action.equals("removePattern") && request.getParameter("pattern") != null) {
					blacklist.removePattern(request.getParameter("pattern"));
					response.sendRedirect("/blacklist?list=" + URLEncoder.encode(blacklist.name, "UTF-8"));
					return null;
				} else if (action.equals("editPattern") && request.getParameter("fromPattern") != null && request.getParameter("toPattern") != null) {
					blacklist.editPattern(request.getParameter("fromPattern"), request.getParameter("toPattern"));
					response.sendRedirect("/blacklist?list=" + URLEncoder.encode(blacklist.name, "UTF-8"));
					return null;
				}
			} else if (request.getParameter("fromPattern") != null)
				context.put("fromPattern", request.getParameter("fromPattern"));

			context.put("curList", blacklist); 

		} catch( Exception e ) {
			logger.warn("Unexpected Error:", e);
		}
		return template;
	}
}
