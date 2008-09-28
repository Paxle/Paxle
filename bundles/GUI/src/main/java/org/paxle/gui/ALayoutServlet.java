package org.paxle.gui;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.servlet.VelocityLayoutServlet;

public abstract class ALayoutServlet extends VelocityLayoutServlet {
    private static final long serialVersionUID = 1L;
    public static final String SERVICE_MANAGER = "manager";
	
    /**
     * Logger
     */
    protected Log logger = LogFactory.getLog(this.getClass());
    
    /**
     * The location of the OSGi bundle this servlet belongs to.
     * This string is passed to the velocity jar-resource-loader
     * as <code>jar.resource.loader.path</code>.
     */
	protected String bundleLocation = null;
	
	/**
	 * @param bundleLocation the location of the osgi bundle the servlet belongs to.<br />
	 *        e.g. <code>file:/path-to-the-bundle-jar/FilterBlacklist-0.0.1.jar</code><br />
	 *        Just call <code>bundlecontext.getBundle().getLocation();</code> to get it.
	 * @param servletLocation the relative location where this servlet is registered at the
	 *        manager, e.g. <code>/search</code>.
	 */
	public void setBundleLocation(final String bundleLocation) {
		this.bundleLocation = bundleLocation;
		if (this.bundleLocation != null && this.bundleLocation.endsWith("/")) {
			this.bundleLocation = this.bundleLocation.substring(0,this.bundleLocation.length()-1);
		}
	}
	
	/**
	 * This function is required to set the velocity jar-resource-loader path to the bundle location
	 * Don't remove this!
	 */
	@Override
	protected ExtendedProperties loadConfiguration(ServletConfig config) throws IOException {
		ExtendedProperties props = super.loadConfiguration(config);
		
		if (this.bundleLocation != null) {
//			props.addProperty("jar.resource.loader.path", "jar:" + this.bundleLocation + ",jar:" + config.getInitParameter("bundle.location"));
			props.addProperty("url.resource.loader.root", this.bundleLocation);
		} 
		if (config.getInitParameter("bundle.location") != null) {
//			props.addProperty("jar.resource.loader.path", "jar:" + config.getInitParameter("bundle.location"));
			props.addProperty("url.resource.loader.root",this.bundleLocation+ "," + config.getInitParameter("bundle.location"));
		}
		return props;
	}
	
	@Override
	public abstract Template handleRequest( HttpServletRequest request, HttpServletResponse response, Context context) throws Exception;
}
