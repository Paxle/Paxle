package org.paxle.filter.blacklist;

import java.net.URLEncoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;
import org.paxle.filter.blacklist.impl.BlacklistFilter;

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
       		if (request.getMethod().equals("POST") && action.equals("addList") && request.getParameter("listName") != null) {
       			if  (! listnames.contains(request.getParameter("listName")))
       					blacklistFilter.addList(request.getParameter("listName"));
     			response.sendRedirect("/blacklist?list=" + URLEncoder.encode(request.getParameter("listName"), "UTF-8"));
     			return null;
       		}
       		
       		// get the current list
        	String curList = request.getParameter("list");
        	
        	// check if this list exists:
        	if (! listnames.contains(curList))
        		curList = null;
        	
        	if (curList == null) {
        		if (! this.blacklistFilter.getLists().isEmpty()) // get the first list when there is one
        			curList = this.blacklistFilter.getLists().get(0);
        		else if (! action.equals("newList")) { // well, it seems there aren't any lists, so the user might want to create one, let's redirect him
        			response.sendRedirect("/blacklist?action=newList");
        			return null;
        		}
        	} else if (request.getMethod().equals("POST")) { // these are non-save methods and should only be executed when there is a post-request and a valid list was given
        		if (action.equals("removeList")) {
        			blacklistFilter.removeList(curList);
        			response.sendRedirect("/blacklist");
        			return null;
        		} else if (action.equals("addPattern") && request.getParameter("pattern") != null) {
        			blacklistFilter.addPattern(request.getParameter("pattern"), curList);
        			response.sendRedirect("/blacklist?list=" + URLEncoder.encode(curList, "UTF-8"));
        			return null;
        		}
       		}
       		
        	context.put("curList", curList); 
       		
       		// when we get until here, the user probably wants to display the list, so get the items
        	if (curList != null)
        		context.put("patternList", blacklistFilter.getPatternList(curList));
       		
        	template = this.getTemplate("/resources/templates/Blacklist.vm");
        } catch( Exception e ) {
        	e.printStackTrace();
        } catch (Error e) {
        	e.printStackTrace();
        }
        return template;
    }
}
