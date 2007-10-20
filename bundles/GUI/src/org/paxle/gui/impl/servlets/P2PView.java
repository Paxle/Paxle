package org.paxle.gui.impl.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.AServlet;
import org.paxle.gui.impl.ServiceManager;

public class P2PView extends AServlet {
	
	private static final long serialVersionUID = 1L;
	
	public P2PView(ServiceManager manager) {
		super(manager);
	}

    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response,
                                   Context context ) {

    	Template template = null;

    	try {
    		/*
    		 * Setting template parameters
    		 */
//    		context.put("manager", this.manager);                 
    		template = this.getTemplate("/resources/templates/p2p.vm");
    	} catch( Exception e ) {
    		e.printStackTrace();
    	} catch (Error e) {
    		e.printStackTrace();
    	}

        return template;
    }

}
