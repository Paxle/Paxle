package org.paxle.gui.impl.servlets;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.osgi.framework.Bundle;
import org.osgi.service.metatype.AttributeDefinition;
import org.paxle.gui.ALayoutServlet;
import org.paxle.gui.impl.StyleManager;


public class SettingsView extends ALayoutServlet
{

	/**
	 * allows other bundles to
	 */

	private static final long serialVersionUID = 1L;
	
	/**
	 * A mapping between type-nr and type string
	 */
    private static final Map<Integer,String> dataTypes = new HashMap<Integer,String>();
    static {
    	dataTypes.put(Integer.valueOf(AttributeDefinition.BOOLEAN), "Boolean");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.BYTE),"Byte");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.CHARACTER),"Character");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.DOUBLE),"Double");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.FLOAT),"Float");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.INTEGER),"Integer");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.LONG),"Long");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.SHORT),"Short");
    	dataTypes.put(Integer.valueOf(AttributeDefinition.STRING),"String");
    }	
    
    /**
     * For logging
     */
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
			context.put( "dataTypes", dataTypes);
			
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
