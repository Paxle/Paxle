/**
 * This file is part of the Paxle project.
 * Visit http://www.paxle.net for more information.
 * Copyright 2007-2008 the original author or authors.
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

import javax.servlet.ServletConfig;

import org.apache.velocity.tools.view.JeeConfig;
import org.apache.velocity.tools.view.VelocityView;
import org.paxle.gui.IServiceManager;
import org.paxle.gui.IVelocityViewFactory;

public class VelocityViewFactory implements IVelocityViewFactory {
	private IServiceManager sm;
	
	public VelocityViewFactory(IServiceManager serviceManager) {
		this.sm = serviceManager;
	}

	public VelocityView createVelocityView(ServletConfig config) {
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		VelocityView view = new PaxleVelocityView(new JeeConfig(config));
		if (oldCl != null) Thread.currentThread().setContextClassLoader(oldCl);
		
		config.getServletContext().setAttribute("manager", this.sm);		
		return view;
	}

}
