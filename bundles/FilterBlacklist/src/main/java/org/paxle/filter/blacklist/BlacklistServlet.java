package org.paxle.filter.blacklist;

import java.net.URLEncoder;

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
        	context.put("listnames", this.blacklistFilter.getLists());
        	
        	// get the action
        	String action = request.getParameter("action");
        	if (action == null)
        		action = "list";
        	context.put("action", action);
        	
        	// create a new list
       		if (request.getMethod().equals("POST") && action.equals("addList") && request.getParameter("listName") != null) {
     			blacklistFilter.addList(request.getParameter("listName"));
     			response.sendRedirect("/blacklist?list=" + URLEncoder.encode(request.getParameter("listName"), "UTF-8"));
     			return null;
       		}
       		
       		// delete a list
       		if (request.getMethod().equals("POST") && action.equals("removeList") && request.getParameter("listName") != null) {
       			blacklistFilter.removeList(request.getParameter("listName"));
       			response.sendRedirect("/blacklist");
       			return null;
       		}
       		
       		// add a new pattern
       		if (request.getMethod().equals("POST") && action.equals("addPattern") && request.getParameter("listName") != null) {
       			blacklistFilter.addPattern(request.getParameter("pattern"), request.getParameter("listName"));
       			response.sendRedirect("/blacklist?list=" + URLEncoder.encode(request.getParameter("listName"), "UTF-8"));
       			return null;
       		}
       		
        	// get the current list
        	String curList = request.getParameter("list");
        	// when there is no list specified, take the first one
        	if (curList == null) {
        		if (! this.blacklistFilter.getLists().isEmpty())
        			curList = this.blacklistFilter.getLists().get(0);
        		else if (! action.equals("newList")) { // well, it seems there aren't any lists, so the user might want to create one, let's redirect him
        			response.sendRedirect("/blacklist?action=newList");
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
