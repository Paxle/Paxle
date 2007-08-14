package org.paxle.gui.impl;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.doc.*;

public class SearchView extends AServlet {

    public SearchView(ServiceManager manager) {
        super(manager);
    }
    
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {
        
        Template template = null;
        
        try {
            if (request.getParameter("query") != null) {
                context.put("searchQuery", request.getParameter("query"));
            }
            context.put("manager", this.manager);
            context.put("url", IIndexerDocument.LOCATION);
            template = this.getTemplate("/resources/templates/SearchView.vm");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return template;
    }
}
