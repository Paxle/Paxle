
package org.paxle.desktop;

import java.awt.Container;
import java.awt.Dimension;

public interface DIComponent {
	
	public void close();
	public abstract String getTitle();
	public abstract Dimension getWindowSize();
	public abstract Container getContainer();
}
