
package org.paxle.desktop;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;

public interface DIComponent {
	
	public abstract void close();
	public abstract String getTitle();
	public abstract Dimension getWindowSize();
	public abstract Container getContainer();
	public abstract void setFrame(Frame frame);
}
