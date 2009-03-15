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
package org.paxle.gui.impl;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.metatype.MetaTypeProvider;
import org.osgi.service.useradmin.Group;
import org.osgi.service.useradmin.Role;
import org.osgi.service.useradmin.User;
import org.osgi.service.useradmin.UserAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.paxle.core.io.IResourceBundleTool;
import org.paxle.gui.IMenuManager;
import org.paxle.gui.IServletManager;
import org.paxle.gui.IStyleManager;
import org.paxle.gui.impl.tools.MetaDataTool;

public class Activator implements BundleActivator {

	private MenuManager menuManager = null;

	private ServletManager servletManager = null;

	private ServiceTracker userAdminTracker = null;

	public void start(BundleContext bc) throws Exception {
		// initialize service Manager for toolbox usage (don't remove this!)
		// XXX how can we initialize this without using a static field?
		ServiceManager.context = bc;
		MetaDataTool.context = bc;

		// init user administration
		this.initUserAdmin(bc);

		// servlet manager
		this.servletManager = this.createAndInitServletManager(bc);
		
		// menu manager
		this.menuManager = new MenuManager(this.servletManager);
		bc.registerService(IMenuManager.class.getName(),this.menuManager, null);		

		// register classloader
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

		/*
		 * ========================================================== 
		 * Register Service Listeners
		 * ==========================================================
		 */
		bc.addServiceListener(new ServletListener(servletManager, menuManager, userAdminTracker, bc), ServletListener.FILTER);
		bc.addServiceListener(new HttpServiceListener(servletManager, bc), HttpServiceListener.FILTER);

		// TODO: allow to globally disable authentication ...
		HttpContextAuth httpAuth = null;
		if (System.getProperty("org.paxle.gui.auth.skip") == null) {		
			httpAuth = new HttpContextAuth(bc.getBundle(), this.userAdminTracker);
		}
	}

	@SuppressWarnings("unchecked")
	private void initUserAdmin(BundleContext context) throws IOException {
		// create a tracker used by the http-context
		this.userAdminTracker = new ServiceTracker(context, UserAdmin.class.getName(), null);
		this.userAdminTracker.open();

		// create a admin role if needed
		UserAdmin userAdmin = (UserAdmin) this.userAdminTracker.getService();
		if (userAdmin != null) {
			// check if an Administrator group is already available
			Group admins = (Group) userAdmin.getRole("Administrators");
			if (admins == null) {
				admins = (Group) userAdmin.createRole("Administrators",Role.GROUP);
			}

			User admin = (User) userAdmin.getRole("Administrator");
			if (admin == null) {
				// create a default admin user
				admin = (User) userAdmin.createRole("Administrator", Role.USER);
				admins.addMember(admin);

				// configure http-login data
				Dictionary<String, Object> props = admin.getProperties();
				props.put(HttpContextAuth.USER_HTTP_LOGIN, "admin");

				Dictionary<String, Object> credentials = admin.getCredentials();
				credentials.put(HttpContextAuth.USER_HTTP_PASSWORD, "");
			}
		}
	}
	
	private ServletManager createAndInitServletManager(BundleContext bc) {
		// creating component
		ServletManager sManager = new ServletManager(bc.getBundle().getEntry("/").toString());
		
		// managed- and metatype-provider-service properties
		Hashtable<String, Object> props = new Hashtable<String, Object>();
		props.put(Constants.SERVICE_PID, ServletManager.PID);		
		
		// register the filter-manager as service
		bc.registerService(IServletManager.class.getName(), sManager, null);
		bc.registerService(new String[]{ManagedService.class.getName()}, sManager, props);				
		
		return sManager;
	}

	public void stop(BundleContext context) throws Exception {
		// unregister all servlets
		servletManager.close();

		this.userAdminTracker.close();

		// cleanup
		ServiceManager.context = null;
		MetaDataTool.context = null;
		this.servletManager = null;
		this.menuManager = null;
	}
}