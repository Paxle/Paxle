/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
 * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
 * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
 * or in the file LICENSE.txt in the root directory of the Paxle distribution.
 *
 * Unless required by applicable law or agreed to in writing, this software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package org.paxle.gui.impl.tools;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.ServletContext;

import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.ResourceTool;
import org.apache.velocity.tools.view.ViewContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.gui.IVelocityViewFactory;

@DefaultKey(MonitorableTool.TOOL_NAME)
@ValidScope(Scope.REQUEST)
public class ResourceBundleTool extends ResourceTool {
	public static final String TOOL_NAME = "resourceTool";
	
	private PaxleLocaleConfig localeConfig;
	
	/**
	 * A tool to load {@link ResourceBundle resource-bundles} from {@link Bundle OSGi-bundles}
	 */
	private IResourceBundleTool resourceBundleTool;
	
	/**
	 * The OSGi-Bundle the currently invoked servlet belongs to
	 */
	private Bundle servletBundle;
	
	/**
	 * The OSGi-Bundle the GUI belongs to.
	 * We require this bundle as a fallback for resource-bundle loading
	 */
	private Bundle guiBundle;
	
	/**
	 * The context of the {@link #servletBundle OSGi-bundle}
	 */
	private BundleContext context;
	
	public void configure(@SuppressWarnings("unchecked") Map props) {
		super.configure(props);		
		if (props != null) {
			// reading the locale to use
			this.localeConfig = new PaxleLocaleConfig();
			this.localeConfig.configure(props);
			
			// getting the current osgi-bundle and -context
			final ServletContext servletContext = (ServletContext) props.get(ViewContext.SERVLET_CONTEXT_KEY);
			this.context = (BundleContext) servletContext.getAttribute(IVelocityViewFactory.BUNDLE_CONTEXT);
			this.servletBundle = this.context.getBundle();
			
			// getting the GUI bundle
			final Bundle[] bundles = this.context.getBundles();
			for (Bundle bundle : bundles) {
				if (bundle.getSymbolicName().equalsIgnoreCase("org.paxle.gui")) {
					this.guiBundle = bundle;
					break;
				}
			}
			
			// getting the resource-bundle-tool
			final ServiceReference ref = this.context.getServiceReference(IResourceBundleTool.class.getName());
			if (ref != null) {
				this.resourceBundleTool = (IResourceBundleTool) this.context.getService(ref);
			}
		}
	}	
	
	@Override
	public Locale getLocale() {
		return this.localeConfig.getLocale();
	}
	
	@Override
	protected ResourceBundle getBundle(String baseName, Object loc) {
        final Locale locale = (loc == null) ? getLocale() : toLocale(loc);
        if (baseName == null || locale == null) {
            return null;
        }
		
        // trying to load the resource-bundle from the servlet OSGi-bundle
        ResourceBundle rb = this.resourceBundleTool.getLocalization(this.servletBundle, baseName, locale);
        if (rb == null ) {
        	// trying to load the resource-bundle from the GUI OSGi-bundle
        	rb = this.resourceBundleTool.getLocalization(this.guiBundle, baseName, locale);
        }
        return rb;
	}
	
	/**
	 * This function was copied from {@link ResourceTool} because it was declared
	 * as private but is needed within {@link #getBundle(String, Object)}
	 * 
	 * @param obj
	 * @return
	 */
    private Locale toLocale(Object obj)
    {
        if (obj == null)
        {
            return null;
        }
        if (obj instanceof Locale)
        {
            return (Locale)obj;
        }
        String s = String.valueOf(obj);
        return ConversionUtils.toLocale(s);
    }	
}
