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

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.ConversionUtils;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.ResourceTool;

@ValidScope(Scope.REQUEST)
public class ResourceBundleTool extends ResourceTool {
	private PaxleLocaleConfig localeConfig;
	private ClassLoader cl;
	
	public void configure(@SuppressWarnings("unchecked") Map props) {
		super.configure(props);		
		if (props != null) {
			// reading the locale to use
			this.localeConfig = new PaxleLocaleConfig();
			this.localeConfig.configure(props);
			
			// the classoader-the current servlet was loaded with
			VelocityEngine engine = (VelocityEngine) props.get("velocityEngine");
			this.cl = (ClassLoader) engine.getApplicationAttribute("servlet.classloader");
		}
	}	
	
	@Override
	public Locale getLocale() {
		return this.localeConfig.getLocale();
	}

	@Override
    public Object get(Object k, String baseName, Object l)
    {
        if (baseName == null || k == null)
        {
            return null;
        }
        String key = k == null ? null : String.valueOf(k);
        Locale locale;
        if (l == null)
        {
            locale = getLocale();
        }
        else
        {
            locale = toLocale(l);
            // if conversion fails, return null to indicate an error
            if (locale == null)
            {
                return null;
            }
        }

        ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, this.cl);
        if (bundle != null)
        {
            try
            {
                return bundle.getObject(key);
            }
            catch (Exception e)
            {
                // do nothing
            }
        }
        return null;
    }
	
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
