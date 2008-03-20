package org.paxle.gui;

import java.util.Collection;

public interface IStyleManager {
	public void searchForStyles();
	public void setStyle(String name);
	public Collection<String> getStyles();
}
