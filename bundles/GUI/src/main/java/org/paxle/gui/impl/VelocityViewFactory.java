package org.paxle.gui.impl;

import javax.servlet.ServletConfig;

import org.apache.velocity.tools.view.JeeConfig;
import org.apache.velocity.tools.view.VelocityView;
import org.paxle.gui.IVelocityViewFactory;

public class VelocityViewFactory implements IVelocityViewFactory {

	public VelocityView createVelocityView(ServletConfig config) {
		ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
		
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		VelocityView view = new PaxleVelocityView(new JeeConfig(config));
		if (oldCl != null) Thread.currentThread().setContextClassLoader(oldCl);
		
		return view;
	}

}
