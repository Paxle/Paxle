package org.paxle.gui.impl;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.core.doc.IIndexerDocument;
import org.paxle.se.search.ISearchProviderManager;
import org.paxle.se.search.ISearchResult;

public class SearchView extends AServlet {

    protected Log logger = LogFactory.getLog(SearchView.class);
    
    public SearchView(ServiceManager manager) {
        super(manager);
    }
    
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {
        
        Template template = null;

        try {
            String testquery = "test";
            if (request.getParameter("query") != null) {
                //context.put("searchQuery", request.getParameter("query"));
                testquery = request.getParameter("query");
            }
            context.put("manager", this.manager);
            context.put("url", IIndexerDocument.LOCATION);
            template = this.getTemplate("/resources/templates/SearchView.vm");
            
            
            //testing only
            ISearchProviderManager Search = (ISearchProviderManager) manager.getService("org.paxle.se.search.ISearchProviderManager");
            this.logger.debug("testquery: " + testquery);
            List<ISearchResult> bla = Search.search(testquery, 10, 10000);
            this.logger.debug(bla.size());
            Iterator<ISearchResult> blubb = bla.iterator();
            while(blubb.hasNext()) {
                ISearchResult blubb2 = blubb.next();
                this.logger.debug("blubb2-size = "+blubb2.getResult().length);
                this.logger.debug("blubb2-searchtime = "+blubb2.getSearchTime());
            }
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            this.logger.error("Error",e);
        }
        return template;
    }
}
