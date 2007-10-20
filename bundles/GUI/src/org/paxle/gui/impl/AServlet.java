package org.paxle.gui.impl;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityLayoutServlet;

public class AServlet extends VelocityLayoutServlet {
	
    private static final long serialVersionUID = 1L;
    /**
	 * Manager class to access other components registered via OSGi
	 */
	protected ServiceManager manager = null;
	
	public AServlet(ServiceManager manager) {
		this.manager = manager;
	}
	
	/**
	 * This function is required to set the velocity jar-resource-loader path to the bundle location
	 * Don't remove this!
	 */
	@Override
	protected ExtendedProperties loadConfiguration(ServletConfig config) throws IOException {
		ExtendedProperties props = super.loadConfiguration(config);
		props.addProperty("jar.resource.loader.path", "jar:" + config.getInitParameter("bundle.location"));
		return props;
	}
	
	@Override
	protected Context createContext(HttpServletRequest request, HttpServletResponse response) {
		Context context = super.createContext(request, response);
		
		// adding service manager to context
		context.put("manager", this.manager);
		
		return context;
	}
}
