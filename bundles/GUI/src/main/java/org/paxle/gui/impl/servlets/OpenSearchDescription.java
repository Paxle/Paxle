package org.paxle.gui.impl.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;

public class OpenSearchDescription extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
    public Template handleRequest( 
    		HttpServletRequest request,
            HttpServletResponse response,
            Context context 
    ) {

    	Template template = null;
		try {
			template = this.getTemplate("/resources/opensearch/OpenSearchDescription.vm");
		} catch (Exception e) {
			this.logger.error("Error",e);
		}

        context.put("layout", "plain.vm");

        return template;
    }

}
