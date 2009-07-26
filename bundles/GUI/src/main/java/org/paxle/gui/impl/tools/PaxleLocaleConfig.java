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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.tools.Scope;
import org.apache.velocity.tools.config.ValidScope;
import org.apache.velocity.tools.generic.LocaleConfig;
import org.apache.velocity.tools.view.CookieTool;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.useradmin.User;
import org.paxle.gui.impl.ServletManager;

@ValidScope(Scope.REQUEST)
public class PaxleLocaleConfig extends LocaleConfig {
	protected BundleContext context;

	/**
	 * For logging
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	
	/**
	 * Currently logged in user
	 */
	protected User user;
	
	protected String l10n;
	
	protected CookieTool cookieTool;

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
		super.configure(props);
		if (props != null) {
			final HttpSession session = (HttpSession) props.get("session");
			final HttpServletRequest request = (HttpServletRequest) props.get("request");
			final HttpServletResponse response = (HttpServletResponse) props.get("response");
			
			// getting the bundle context
			final ServletContext servletContext = (ServletContext) props.get("servletContext");			
			this.context = (BundleContext) servletContext.getAttribute("bc");
			
			// getting the configured user-language (if specified)			
			if (session != null) {
				this.user = (User) session.getAttribute(HttpContext.REMOTE_USER);
				if (user != null) {
					this.l10n = (String) user.getProperties().get("user.language");
				} 
			}
			
			// getting the language parameter from cookies			
			this.cookieTool = new CookieTool();
			this.cookieTool.setRequest(request);
			this.cookieTool.setResponse(response);
			if (this.cookieTool.get("l10n") != null) {
				this.l10n = this.cookieTool.get("l10n").getValue();
			}
			
			// getting the language parameter (if specified)
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
	
	@SuppressWarnings("unchecked")
	public void setLocaleStr(String locale) {
		if (this.user != null) {
			// updating user properties
			user.getProperties().put("user.language", locale);
		} 
		
		// keep cookies in sync
		this.cookieTool.add("l10n", locale);
	}
	
	public BundleContext getBundleContext() {
		return this.context;
	}
	
	protected Bundle getBundleByBundleID(Integer bundleID) {
		// getting a reference to the requested bundle
		final Bundle bundle = this.context.getBundle(bundleID.longValue());
		if (bundle != null) return bundle;
		
		this.logger.warn(String.format(
				"No bundle found for ID '%d'.",
				bundleID
		));
		return null;
	}	
	
	protected Bundle getBundleByServicePID(String servicePID) {
		ServiceReference[] refs = null;
		try {
			refs = context.getAllServiceReferences(null, String.format("(%s=%s)",Constants.SERVICE_PID,servicePID));
			if (refs != null && refs.length > 0) {
				ServiceReference serviceRef = refs[0];
				return serviceRef.getBundle();
			}
			return null;
		} catch (Exception e) {
			this.logger.warn(String.format("No bundle found providing service '%s'.", servicePID));
		} finally {
			if (refs != null) for(ServiceReference ref : refs) context.ungetService(ref);
		}
		return null;
	}	
}
