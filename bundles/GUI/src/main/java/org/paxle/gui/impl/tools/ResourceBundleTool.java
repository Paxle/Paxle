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
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.ToolContext;
import org.apache.velocity.tools.config.DefaultKey;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.ResourceTool;
import org.paxle.gui.IVelocityViewFactory;

@DefaultKey(MonitorableTool.TOOL_NAME)
@ValidScope(Scope.REQUEST)
public class ResourceBundleTool extends ResourceTool {
	public static final String TOOL_NAME = "resourceTool";
	
	private PaxleLocaleConfig localeConfig;
	private ClassLoader cl;
	
	public void configure(@SuppressWarnings("unchecked") Map props) {
		super.configure(props);		
		if (props != null) {
			// reading the locale to use
			this.localeConfig = new PaxleLocaleConfig();
			this.localeConfig.configure(props);
			
			// the classoader-the current servlet was loaded with
			final VelocityEngine engine = (VelocityEngine) props.get(ToolContext.ENGINE_KEY);
			this.cl = (ClassLoader) engine.getApplicationAttribute(IVelocityViewFactory.SERVLET_CLASSLOADER);
		}
	}	
	
	@Override
	public Locale getLocale() {
		return this.localeConfig.getLocale();
	}
	
	@Override
	protected ResourceBundle getBundle(String baseName, Object loc) {
        Locale locale = (loc == null) ? getLocale() : toLocale(loc);
        if (baseName == null || locale == null) {
            return null;
        }
		
		try {
			// trying to load the resource using the servlet classloader
			return ResourceBundle.getBundle(baseName, locale, this.cl);
		} catch (MissingResourceException e) {
			// trying to fallback to the classloader of this tool class
			return ResourceBundle.getBundle(baseName, locale, this.getClass().getClassLoader());
		}
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
