package org.paxle.gui.impl.servlets;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.paxle.gui.ALayoutServlet;

public class TheaddumpView extends ALayoutServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public Template handleRequest( 
			HttpServletRequest request,
			HttpServletResponse response,
			Context context 
	) {
		
		Template template = null;
		
		try {
			// get the current dump
			Map<Thread,StackTraceElement[]> dumps = Thread.getAllStackTraces();
			context.put("dumps", dumps);
			
			// specify the template to use
			template = this.getTemplate("/resources/templates/ThreaddumpView.vm");            
		} catch (Exception e) {
			// TODO Auto-generated catch block
			this.logger.error("Error",e);
		}
		return template;
	}
}
