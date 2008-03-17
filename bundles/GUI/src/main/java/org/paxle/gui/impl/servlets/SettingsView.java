package org.paxle.gui.impl.servlets;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.StyleManager;


public class SettingsView extends ALayoutServlet
{

	/**
	 * allows other bundles to
	 */

	private static final long serialVersionUID = 1L;

	private Log logger = LogFactory.getLog( this.getClass());

	@Override
	public Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context)
	{

		Template template = null;
		
		if( request.getParameter( "searchForStyles") != null) {
			StyleManager.searchForStyles();
		}
		
		if( request.getParameter( "changeStyle") != null) {
			StyleManager.setStyle( request.getParameter( "changeStyle"));
		}
		

		try {
			context.put( "availbleStyles", StyleManager.getStyles());
			template = this.getTemplate( "resources/templates/SettingsView.vm");
		} catch (ResourceNotFoundException e) {
			logger.error( "resource: " + e);
			e.printStackTrace();
		} catch (ParseErrorException e) {
			logger.error( "parse : " + e);
			e.printStackTrace();
		} catch (Exception e) {
			logger.error( "exception: " + e);
			e.printStackTrace();
		}

		return template;
	}
}
