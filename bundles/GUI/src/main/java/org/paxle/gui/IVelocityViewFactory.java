package org.paxle.gui;

import javax.servlet.ServletConfig;

import org.apache.velocity.tools.view.VelocityView;

public interface IVelocityViewFactory {
	public VelocityView createVelocityView(ServletConfig config);
}
