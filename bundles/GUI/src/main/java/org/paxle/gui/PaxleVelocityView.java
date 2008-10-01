package org.paxle.gui;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.tools.view.JeeConfig;
import org.apache.velocity.tools.view.VelocityView;

public class PaxleVelocityView extends VelocityView {

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