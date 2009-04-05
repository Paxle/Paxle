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

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.LocaleConfig;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.User;
import org.paxle.gui.impl.ServletManager;

@ValidScope(Scope.REQUEST)
public class PaxleLocaleConfig extends LocaleConfig {
	protected BundleContext context;

	protected String l10n;

	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());

	/**
	 * This method is called by velocity during tool(box) initialization
	 * We need it to fetch a reference to the current {@link BundleContext} from the {@link ServletContext}.
	 * 
	 * The {@link BundleContext} was added to the {@link ServletContext} by the {@link ServletManager} during
	 * {@link Servlet} registration.
	 * 
	 * @param props
	 */
	@Override
	public void configure(@SuppressWarnings("unchecked") Map props) {
		if (props != null) {
			// getting the bundle context
			ServletContext servletContext = (ServletContext) props.get("servletContext");			
			this.context = (BundleContext) servletContext.getAttribute("bc");
			
			// getting the configured user-language (if specified)
			HttpSession session = (HttpSession) props.get("session");
			if (session != null) {
				User user = (User) session.getAttribute(HttpContext.REMOTE_USER);
				if (user != null) {
					this.l10n = (String) user.getProperties().get("user.language");
				}
			}
			
			// getting the language parameter (if specified)
			HttpServletRequest request = (HttpServletRequest) props.get("request");
			if (request.getParameter("l10n") != null) {
				this.l10n = request.getParameter("l10n");
			}
		}
	}
	
	@Override
	public Locale getLocale() {
		// checking if the language was set via request-parameter or user-config
		if (this.l10n != null) {
			return new Locale(this.l10n);
		}
		
		// just use the language header of the request
		return super.getLocale();
	}	
	
	public String getLocaleStr() {
		Locale locale = this.getLocale();
		return (locale == null) ? "en" : locale.toString();
	}
	
	public BundleContext getBundleContext() {
		return this.context;
	}
}
