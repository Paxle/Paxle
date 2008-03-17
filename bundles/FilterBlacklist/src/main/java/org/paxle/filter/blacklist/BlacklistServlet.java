package org.paxle.filter.blacklist;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;

public class BlacklistServlet extends ALayoutServlet {

	private static final long serialVersionUID = 1L;

    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {

        Template template = null;
        try {        	
        	template = this.getTemplate("/resources/templates/Blacklist.vm");
        } catch( Exception e ) {
        	e.printStackTrace();
        } catch (Error e) {
        	e.printStackTrace();
        }
        return template;
    }
}
