/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2010 the original author or authors.
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

import javax.annotation.Nonnull;
import javax.servlet.ServletConfig;

import org.apache.velocity.tools.view.JeeServletConfig;
import org.apache.velocity.tools.view.VelocityView;
import org.osgi.framework.BundleContext;
import org.paxle.gui.IServletManager;
import org.paxle.gui.IVelocityViewFactory;

public class VelocityViewFactory implements IVelocityViewFactory {
	private BundleContext bc;
	private IServletManager sm;
	
	public VelocityViewFactory(@Nonnull BundleContext bc, @Nonnull IServletManager sm) {
		this.bc = bc;
		this.sm = sm;
	}

	public VelocityView createVelocityView(ServletConfig config) {
		// remember old classloader
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		
		// create and init a new velocity view
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		VelocityView view = new PaxleVelocityView(new JeeServletConfig(config));
		
		// re-set old classloader
		if (oldCl != null) Thread.currentThread().setContextClassLoader(oldCl);
		
		// put the bundle-context into the servlet-context to allow
		// custom tools to access it
		config.getServletContext().setAttribute(BUNDLE_CONTEXT, this.bc);
		config.getServletContext().setAttribute(SERVLET_MANAGER, this.sm);
		
		return view;
	}

}
