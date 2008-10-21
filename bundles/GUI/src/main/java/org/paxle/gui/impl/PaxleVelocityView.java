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

import java.util.Hashtable;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.JeeConfig;
import org.apache.velocity.tools.view.VelocityView;

public class PaxleVelocityView extends VelocityView {

	private Hashtable<String, String> props;
	
	public PaxleVelocityView(JeeConfig config) {
		super(config);
	}

	protected void configure(JeeConfig config, VelocityEngine engine) {
		super.configure(config, engine);
		
		String bundleLocationDefault = config.getInitParameter("bundle.location.default");
		
		String bundleLocation = config.getInitParameter("bundle.location");
		if (bundleLocation != null) { 
			engine.setProperty("url.resource.loader.root", bundleLocationDefault + "," + bundleLocation);
		} else {
			engine.setProperty("url.resource.loader.root", bundleLocationDefault);
		}
	}
} 