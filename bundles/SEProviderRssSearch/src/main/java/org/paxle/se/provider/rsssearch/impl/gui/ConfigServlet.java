package org.paxle.se.provider.rsssearch.impl.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.se.provider.rsssearch.impl.RssSearchProvider;

public class ConfigServlet extends ALayoutServlet {

	private static final long serialVersionUID = 1L;

	public ConfigServlet() {
	}

	@Override
	public Template handleRequest( HttpServletRequest request,
			HttpServletResponse response,
			Context context ) {
		Template template = null;
		try {
			template = this.getTemplate("/resources/templates/config.vm");
			
			List<String> urls = RssSearchProvider.getUrls();
			context.put("urls", urls);
			if(request.getMethod().equals("POST")){
				String[] new_urls=request.getParameter("urls").split("\n");
				ArrayList<String> list=new ArrayList<String>();
				for(int i=0;i<new_urls.length;i++)
					list.add(new_urls[i]);
				RssSearchProvider.setUrls(list);
			}
				
			/*// get the action
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
*/
		} catch( Exception e ) {
			logger.warn("Unexpected Error:", e);
		}
		return template;
	}
}
