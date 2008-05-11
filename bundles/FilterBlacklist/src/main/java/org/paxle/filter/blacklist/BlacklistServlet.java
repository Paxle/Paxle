package org.paxle.filter.blacklist;

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.filter.blacklist.impl.BlacklistFilter;
import org.paxle.filter.blacklist.impl.Blacklist;

public class BlacklistServlet extends ALayoutServlet {

	private static final long serialVersionUID = 1L;
	private BlacklistFilter blacklistFilter = null;

	public BlacklistServlet(BlacklistFilter blacklistFilter) {
		this.blacklistFilter = blacklistFilter;
	}



	public Template handleRequest( HttpServletRequest request,
			HttpServletResponse response,
			Context context ) {
		Template template = null;
		try {
			List<String> listnames = this.blacklistFilter.getLists();
			context.put("listnames", listnames);

			// get the action
			String action = request.getParameter("action");
			if (action == null)
				action = "list";
			context.put("action", action);

			// create a new list
			if (request.getMethod().equals("POST") && action.equals("addList")) {
				if ((request.getParameter("listName") == null) || (request.getParameter("listName").equals(""))) {
					response.sendRedirect("/blacklist");
					return null;
				} else if  (! listnames.contains(request.getParameter("listName"))) {
					blacklistFilter.createList(request.getParameter("listName"));
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
			} else if (request.getMethod().equals("POST")) { // these are non-save methods and should only be executed when there is a post-request and a valid list was given
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
				}
			}

			context.put("curList", blacklist); 

			template = this.getTemplate("/resources/templates/Blacklist.vm");
		} catch( Exception e ) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
		return template;
	}
}
