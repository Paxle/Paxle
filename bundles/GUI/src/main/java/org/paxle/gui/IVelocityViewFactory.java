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
package org.paxle.gui;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;

import org.apache.velocity.tools.view.VelocityView;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public interface IVelocityViewFactory {
	/**
	 * The property-key that can be used insite a Velocity-Tool to access the {@link ClassLoader} of the registered {@link Servlet}.
	 */
	public static final String SERVLET_CLASSLOADER = "servlet.classloader";
	
	/**
	 * The {@link BundleContext} of the OSGi {@link Bundle}, the registered {@link Servlet} belongs to.
	 */
	public static final String BUNDLE_CONTEXT = "bc";
	
	/**
	 * A reference to the {@link IServletManager}
	 */
	public static final String SERVLET_MANAGER = "servletManager";
	
	public VelocityView createVelocityView(ServletConfig config);
}
