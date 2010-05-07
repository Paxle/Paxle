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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.tools.view.VelocityView;
import org.apache.velocity.tools.view.VelocityViewServlet;

public class VelocityViewServletWrapper implements InvocationHandler {
	private final VelocityViewFactory factory;
	private final VelocityViewServlet servlet;
	private final Method setVelocityView;
	
	public VelocityViewServletWrapper(VelocityViewServlet theServlet, VelocityViewFactory factory) throws SecurityException, NoSuchMethodException {
		this.servlet = theServlet;
		this.factory = factory;
		this.setVelocityView = VelocityViewServlet.class.getDeclaredMethod("setVelocityView", VelocityView.class);
		this.setVelocityView.setAccessible(true);
	}
	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			// intercepting: public void init(ServletConfig config)
			if (method.getName().equals("init") && args != null && args.length == 1 && args[0] instanceof ServletConfig) {
				final ServletConfig config = (ServletConfig) args[0];
				
				// init and set the velocity view
				final VelocityView view = factory.createVelocityView(config);
				this.setVelocityView.invoke(this.servlet,view);
			}
			
			// intercepting: service(HttpServletRequest req, HttpServletResponse resp)
			else if (method.getName().equals("service") && args != null && args.length == 2 && args[0] instanceof HttpServletRequest && args[1] instanceof HttpServletResponse) {
				this.setDefaultResponseHeaders((HttpServletResponse) args[1]);
			}
			
			return method.invoke(servlet, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}
	
	protected void setDefaultResponseHeaders(HttpServletResponse resp) {
		resp.setHeader("Server","Paxle");
		resp.setHeader("Pragma","no-cache");
		resp.setHeader("Cache-Control","no-cache, no-store, must-revalidate");
	}
};